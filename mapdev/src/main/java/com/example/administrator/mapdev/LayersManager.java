package com.example.administrator.mapdev;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.RasterLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.ogc.kml.KmlLayer;
import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geodatabase.ShapefileFeature;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.raster.FileRasterSource;
import com.esri.core.renderer.Renderer;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.esri.core.symbol.TextSymbol;

import org.litepal.LitePalApplication;
import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/15.
 * 图层管理类
 */
public class LayersManager {
	static final public double METER_PER_INCH = 0.0254;
	static final public int UNKNOWN_DATA_SOURCE = -1;
	static final public int FILE_RASTER_DATA_SOURCE = 0;
	static final public int FILE_SHP_DATA_SOURCE = 1;
	static final public int FILE_GDB_DATA_SOURCE = 2;
	static final public int FILE_KML_DATA__SOURCE = 3;
	static final public int TOP_LAYER_NUM = 2;
	static final public String USER_FEATURE_DATA_PATH = "/mapdev/output/";
	private MapView mapView;
	private Context context;
	private double screenWidthMeter;
	private List<LayerItemData> layerItems = new ArrayList<>();
	private SQLiteDatabase db = Connector.getDatabase();
	private GraphicsLayer drawerLayer;    //活动图层（所有临时绘制都在活动图层）
	private GraphicsLayer userDrawerLayer;  //用户绘制图层需要序列化保存
	static public SpatialReference wgs84 = SpatialReference.create(SpatialReference.WKID_WGS84);
	private OnStatusChangedListener onStatusChangedListener = null;
//	private FeatureLayer surveyLayer;   //存储实地采集照片的信息(点图层）
//	private FeatureLayer userPolylineLayer; //存储用户绘制的点信息
//	private FeatureLayer userPolygonLayer;  //存储用户绘制的面信息

	public List<LayerItemData> getLayerItems() {
		return layerItems;
	}

	public MapView getMapView() {
		return mapView;
	}

	public GraphicsLayer getDrawerLayer() {
		return drawerLayer;
	}

	public GraphicsLayer getUserDrawerLayer() {
		return userDrawerLayer;
	}

	public double getScreenWidthMeter() {
		return screenWidthMeter;
	}

	public void setScreenWidthMeter(double screenWidthMeter) {
		this.screenWidthMeter = screenWidthMeter;
	}

	public OnStatusChangedListener getOnStatusChangedListener() {
		return onStatusChangedListener;
	}

	public LayersManager setOnStatusChangedListener(OnStatusChangedListener onStatusChangedListener) {
		this.onStatusChangedListener = onStatusChangedListener;
		return this;
	}

	LayersManager(MapView mapView) {
		this.mapView = mapView;
		context = mapView.getContext();
		mapView.setOnStatusChangedListener(StatusChangeListener);
		String dataCache = db.getPath();
		//Toast.makeText(context,dataCache,Toast.LENGTH_LONG).show();
	}

	/**
	 * 从数据库中加载所有图层到当前地图中
	 */
	public void loadMapLayers() {
		//按照图层的优先级次序重新排列图层并加载地图中
		layerItems = DataSupport.order("orderId asc").find(LayerItemData.class);
		//HashMap<String,Object> dataSources=new HashMap<>();
		for (LayerItemData layerItem : layerItems) {
			//Object data_source=dataSources.get(layerItem.getDataSource());
			//if(data_source == null ){
			int data_source_type = getDataSourceTypeByPath(layerItem.getDataSource());
			Layer layer;
			switch (data_source_type) {
				case FILE_RASTER_DATA_SOURCE:
					layer = openRasterLayer(layerItem.getDataSource());
					layerItem.setLayer(layer);
					layer.setVisible(layerItem.getVisible());
					break;
				case FILE_SHP_DATA_SOURCE:
					layer = openVectorLayer(layerItem.getDataSource());
					layerItem.setLayer(layer);
					layer.setVisible(layerItem.getVisible());
					break;
			}
		}
		//loadGeoDatabase("/storage/emulated/0/xihuqu.gdb");
	}

	/**
	 * 重新加载当前地图图层
	 */
	public void reloadMapLayers() {
		mapView.removeAll();
		loadMapLayers();
	}

	public int getDataSourceTypeByPath(String path) {
		File file = new File(path);
		if (file.isFile()) {
			String fileName = file.getName();
			String prefix = fileName.substring(fileName.lastIndexOf(".") + 1);
			if (prefix.equalsIgnoreCase("tiff") || prefix.equalsIgnoreCase("tif") || prefix.equalsIgnoreCase("img")) {
				return FILE_RASTER_DATA_SOURCE;
			} else if (prefix.equalsIgnoreCase("shp")) {
				return FILE_SHP_DATA_SOURCE;
			} else if (prefix.equalsIgnoreCase("gdb")) {
				return FILE_GDB_DATA_SOURCE;
			} else if (prefix.equalsIgnoreCase("kml")) {
				return FILE_KML_DATA__SOURCE;
			}
		}
		return UNKNOWN_DATA_SOURCE;
	}

	private OnStatusChangedListener StatusChangeListener = new OnStatusChangedListener() {
		@Override
		public void onStatusChanged(Object o, STATUS status) {
			if (o instanceof MapView && status == STATUS.INITIALIZED) {
				//初始化一次
				setMapViewOptionsByBaseLayer();
			}
			if (onStatusChangedListener != null)
				onStatusChangedListener.onStatusChanged(o, status);
		}
	};

	private void setMapViewOptionsByBaseLayer() {
		//地图第一次被初始化被调用此函数
		if (!mapView.isLoaded())
			return;
		for (Layer layer : mapView.getLayers()) {
			//重新计算地图范围
			Envelope fullExtent = layer.getFullExtent();
			Envelope projectExtent;
			if (!mapView.getSpatialReference().equals(layer.getDefaultSpatialReference())) {
				projectExtent = (Envelope) GeometryEngine.project(fullExtent, layer.getDefaultSpatialReference(), mapView.getSpatialReference());
			} else {
				projectExtent = fullExtent;
			}
			Envelope mapExtent = mapView.getMaxExtent();
			if (mapExtent != null) {
				mapExtent.merge(projectExtent);
				mapView.setMaxExtent(mapExtent);
			} else {
				mapView.setMaxExtent(projectExtent);
			}
		}

		SpatialReference mapSpatialRef = mapView.getSpatialReference();
		userDrawerLayer = new GraphicsLayer(mapSpatialRef, mapView.getMaxExtent());
		userDrawerLayer.setMinScale(mapView.getMinScale());
		userDrawerLayer.setMaxScale(0);
		mapView.addLayer(userDrawerLayer);
		//GraphicLayer用来绘制临时数据（比如测量等）
		drawerLayer = new GraphicsLayer(mapSpatialRef, mapView.getMaxExtent());
		mapView.addLayer(drawerLayer);
	}

	private void loadUserDrawerLayer() {
		//从缓存数据库中调用用户绘制的数据库文件

	}

	private int getLayerIndex(Layer layer) {
		Layer[] layers = mapView.getLayers();
		for (int i = 0; i < layers.length; i++) {
			if (layers[i] == layer)
				return i;
		}
		return 0;
	}

	/**
	 * 根据索引删除图层
	 *
	 * @param position
	 */
	public void removeLayer(int position) {
		LayerItemData layerItem = layerItems.get(position);
		removeLayer(layerItem);
	}

	public void setLayerVisible(int position, boolean visible) {
		LayerItemData layerItem = layerItems.get(position);
		Layer layer = layerItem.getLayer();
		layer.setVisible(visible);
		layerItem.setVisible(visible);
		layerItem.save();
	}

	public boolean getLayerVisible(int position) {
		LayerItemData layerItem = layerItems.get(position);
		Layer layer = layerItem.getLayer();
		return layer.isVisible();
	}

	public void removeLayer(LayerItemData layerItem) {
		for (LayerItemData layer : layerItems) {
			if (layer.getOrderId() > layerItem.getOrderId()) {
				layer.setOrderId(layer.getOrderId() - 1);
				layer.update(layer.getBaseObjId());
			}
		}
		layerItems.remove(layerItem);
		mapView.removeLayer(layerItem.getLayer());
		//数据库操作
		layerItem.delete();
	}

	/**
	 * 提供给LayerAdpter使用，用于在DragListView中制定位置插入一行
	 *
	 * @param layerItem
	 * @param position
	 */
	public void insertLayer(LayerItemData layerItem, int position) {
		Layer layer = layerItem.getLayer();
		for(int index=position;index<layerItems.size();index++){
				LayerItemData item=layerItems.get(index);
				item.setOrderId(item.getOrderId() + 1);
				//更新数据库
				item.update(item.getBaseObjId());
				//item.save();
		}
//		for (LayerItemData item : layerItems) {
//			if (layerItem.getOrderId() > item.getOrderId()) {
//				item.setOrderId(item.getOrderId() + 1);
//				//更新数据库
//				//item.update(item.getBaseObjId());
//				item.save();
//			}
//		}
		int orderId = mapView.addLayer(layer, position);
		//Layer temp = mapView.getLayer(orderId);
		layerItems.add(position, layerItem);
		layerItem.setOrderId(position);
		layerItem.save();
	}

	public void swapTwoLayersOrder(LayerItemData one, LayerItemData other) {
		swapTwoLayersOrder(one.getOrderId(), other.getOrderId());
		int order = one.getOrderId();
		one.setOrderId(other.getOrderId());
		other.setOrderId(order);
	}

	private void swapTwoLayersOrder(int oneIndex, int otherIndex) {
		Layer layer = mapView.getLayer(oneIndex);
		Layer otherLayer = mapView.getLayer(otherIndex);
		mapView.removeLayer(oneIndex);
		mapView.addLayer(layer, otherIndex);
		mapView.removeLayer(otherIndex);
		mapView.addLayer(otherLayer, oneIndex);
	}

	/**
	 * 保存图层信息到数据库中
	 *
	 * @param layer
	 * @param dataSource
	 */
	private void saveLayerItemData(Layer layer, String dataSource) {
		if (layer instanceof FeatureLayer) {
			LayerItemData layerItem = new LayerItemData();
			layerItem.setOrderId(getLayerIndex(layer));
			layerItem.setDataSource(dataSource);
			layerItem.setLayerType(LayerItemData.FEATURE_LAYER);
			layerItem.setLayer(layer);
			FeatureLayer featureLayer = (FeatureLayer) layer;
			Geometry.Type type = featureLayer.getGeometryType();
			switch (type) {
				case LINE:
				case POLYLINE:
					layerItem.setGeometryType(LayerItemData.POLYLINE);
					break;
				case POINT:
				case MULTIPOINT:
					layerItem.setGeometryType(LayerItemData.POINT);
					break;
				case POLYGON:
					layerItem.setGeometryType(LayerItemData.POLYGON);
					break;
				default:
					layerItem.setGeometryType(LayerItemData.UNKNOWN);
					break;
			}
			layerItems.add(layerItem);
			//保存图层信息数据到数据库
			layerItem.save();
		} else if (layer instanceof RasterLayer) {
			LayerItemData layerItem = new LayerItemData();
			layerItem.setLayer(layer);
			layerItem.setOrderId(getLayerIndex(layer));
			layerItem.setDataSource(dataSource);
			layerItem.setLayerType(LayerItemData.RASTER_LAYER);
			layerItem.setGeometryType(LayerItemData.RASTER);
			layerItems.add(layerItem);
			//保存图层信息数据到数据库
			layerItem.save();
		} else if (layer instanceof KmlLayer) {
			LayerItemData layerItem = new LayerItemData();
			layerItem.setLayer(layer);
			layerItem.setDataSource(dataSource);
			layerItem.setLayerType(LayerItemData.KML_LAYER);
			layerItem.setGeometryType(LayerItemData.UNKNOWN);
			layerItems.add(layerItem);
			//保存图层信息数据到数据库
			layerItem.save();
		}
	}

	private Layer openRasterLayer(String path) {
		FileRasterSource rasterSource = null;
		try {
			rasterSource = new FileRasterSource(path);
			if (mapView.getSpatialReference() != null)
				rasterSource.project(mapView.getSpatialReference());
		} catch (IllegalArgumentException ie) {
			Toast.makeText(context, "null or empty path", Toast.LENGTH_SHORT).show();
		} catch (FileNotFoundException fe) {
			Toast.makeText(context, "raster file doesn't exist", Toast.LENGTH_SHORT).show();
		} catch (RuntimeException re) {
			Toast.makeText(context, "unsupported raster file", Toast.LENGTH_SHORT).show();
		}
		if (rasterSource != null) {
			RasterLayer rasterLayer = new RasterLayer(rasterSource);
			rasterLayer.setOnStatusChangedListener(new OnStatusChangedListener() {
				@Override
				public void onStatusChanged(Object o, STATUS status) {
					if (status == STATUS.LAYER_LOADED || status == STATUS.INITIALIZED) {
						RasterLayer layer = (RasterLayer) o;
						setLayerMinScale(layer);
						zoomToLayerExtent(layer);
					}
				}
			});
			//加载必须在用户图层之后
			if (userDrawerLayer != null) {
				int index = getLayerIndex(userDrawerLayer);
				mapView.addLayer(rasterLayer, index);
			} else {
				mapView.addLayer(rasterLayer);
			}
			return rasterLayer;
		}
		return null;
	}

	public void loadRasterLayer(String path) {
		Layer layer = openRasterLayer(path);
		if (layer != null)
			saveLayerItemData(layer, path);
	}

	private void loadKMLLayer(String path) {
		KmlLayer kmlLayer = new KmlLayer(path);
		mapView.addLayer(kmlLayer);
	}

	/**
	 * 加载gdb数据集
	 *
	 * @param path
	 */
	private void loadGeoDatabase(String path) {
		try {
			Geodatabase geodatabase = new Geodatabase(path, true);
			List<GeodatabaseFeatureTable> featureTables = geodatabase.getGeodatabaseTables();
			for (GeodatabaseFeatureTable featureTable : featureTables) {
				FeatureLayer featureLayer = new FeatureLayer(featureTable);
				mapView.addLayer(featureLayer);
			}
		} catch (FileNotFoundException e) {
			Toast.makeText(context, "geodatabase file doesn't exist", Toast.LENGTH_SHORT).show();
		}
	}

	private Layer openVectorLayer(String path) {
		try {
			ShapefileFeatureTable shapefileFeatureTable = new ShapefileFeatureTable(path);
			FeatureLayer featureLayer = new FeatureLayer(shapefileFeatureTable);
			Geometry.Type type = shapefileFeatureTable.getGeometryType();
			Symbol featureSymbol = null;
			switch (type) {
				//SimpleLineSymbol,TextSymbol
				case LINE:
				case POLYLINE:
					featureSymbol = new SimpleLineSymbol(Color.GREEN, 2.0f);
					break;
				//PictureMarkerSymbol,SimpleMarkerSymbol,TextSymbol
				case POINT:
				case MULTIPOINT:
					featureSymbol = new SimpleMarkerSymbol(Color.MAGENTA, 20, SimpleMarkerSymbol.STYLE.DIAMOND);
					break;
				//SimpleFillSymbol,TextSymbol
				case POLYGON:
					featureSymbol = new SimpleFillSymbol(Color.argb(100, 0, 0, 255));
					break;
				default:
					featureSymbol = new TextSymbol(14, "注记", Color.YELLOW);
					break;
			}
			//SimpleRenderer,UniqueValueRenderer,ClassBreaksRenderer
			Renderer renderer = featureLayer.getRenderer();
			if (renderer == null) {
				renderer = new SimpleRenderer(featureSymbol);
				featureLayer.setRenderer(renderer);
			}
			featureLayer.setOnStatusChangedListener(new OnStatusChangedListener() {
				@Override
				public void onStatusChanged(Object o, STATUS status) {
					if (status == STATUS.LAYER_LOADED || status == STATUS.INITIALIZED) {
						FeatureLayer layer = (FeatureLayer) o;
						setLayerMinScale(layer);
						zoomToLayerExtent(layer);
					}
				}
			});
			//加载必须在用户图层之后
			if (userDrawerLayer != null) {
				int index = getLayerIndex(userDrawerLayer);
				mapView.addLayer(featureLayer, index);
			} else {
				mapView.addLayer(featureLayer);
			}
			return featureLayer;
		} catch (FileNotFoundException e) {
			Toast.makeText(LitePalApplication.getContext(), "vector file doesn't exist", Toast.LENGTH_SHORT).show();
		}
		return null;
	}

	public void loadVectorLayer(String path) {
		Layer layer = openVectorLayer(path);
		if (layer != null)
			saveLayerItemData(layer, path);
	}

	/**
	 * 重新设置地图最小缩放系数
	 *
	 * @param layer
	 */
	private void setLayerMinScale(Layer layer) {
		if (layer.getDefaultSpatialReference() == null) {
			Toast.makeText(context, R.string.layer_spatialref_null, Toast.LENGTH_SHORT).show();
			return;
		}
		Envelope fullExtent = layer.getFullExtent();
		double minScale;
		if (layer.getDefaultSpatialReference().getCoordinateSystemType() == SpatialReference.Type.GEOGRAPHIC) {
			Point centerPt = fullExtent.getCenter();
			double width = 6378137 * Math.toRadians(fullExtent.getWidth()) * Math.cos(Math.toRadians(centerPt.getY()));
			minScale = width / screenWidthMeter;
		} else {
			double width = fullExtent.getWidth();
			minScale = width / screenWidthMeter;
		}
		layer.setMinScale(minScale * 4.5);

		if (mapView.isLoaded()) {
			Envelope projectExtent;
			if (!mapView.getSpatialReference().equals(layer.getDefaultSpatialReference())) {
				projectExtent = (Envelope) GeometryEngine.project(fullExtent, layer.getDefaultSpatialReference(), mapView.getSpatialReference());
			} else {
				projectExtent = fullExtent;
			}
			Envelope mapExtent = mapView.getMaxExtent();
			if (mapExtent != null) {
				mapExtent.merge(projectExtent);
				mapView.setMaxExtent(mapExtent);
			} else {
				mapView.setMaxExtent(projectExtent);
			}
		}
	}

	/**
	 * 缩放地图范围至加载地图范围
	 *
	 * @param layer
	 */
	public void zoomToLayerExtent(Layer layer) {
		if (layer.getDefaultSpatialReference() == null) {
			Toast.makeText(context, R.string.layer_spatialref_null, Toast.LENGTH_SHORT).show();
			return;
		}
		Envelope fullExtent = layer.getFullExtent();
		Point centerPt = fullExtent.getCenter();
		double minScale;
		if (layer.getDefaultSpatialReference().getCoordinateSystemType() == SpatialReference.Type.GEOGRAPHIC) {
			double width = 6378137 * Math.toRadians(fullExtent.getWidth()) * Math.cos(Math.toRadians(centerPt.getY()));
			minScale = width / screenWidthMeter;
		} else {
			double width = fullExtent.getWidth();
			minScale = width / screenWidthMeter;
		}
		if (mapView.isLoaded()) {
			if (!mapView.getSpatialReference().equals(layer.getDefaultSpatialReference())) {
				centerPt = (Point) GeometryEngine.project(centerPt, layer.getDefaultSpatialReference(), mapView.getSpatialReference());
			}
		}
		mapView.zoomToScale(centerPt, minScale * 0.25);
//		mapView.invalidate();
	}

	public Point wgs84ToMapProject(Point point) {
		if (!wgs84.equals(getMapView().getSpatialReference())) {
			Point position = (Point) GeometryEngine.project(point, wgs84, getMapView().getSpatialReference());
			return position;
		} else {
			return point;
		}
	}

	public Point wgs84ToMapProject(GpsLocation location) {
		Point point = new Point(location.getLongitude(), location.getLatitude(), location.getAltitude());
		return wgs84ToMapProject(point);
	}

	public Point wgs84ToMapProject(Location location) {
		Point point = new Point(location.getLongitude(), location.getLatitude(), location.getAltitude());
		return wgs84ToMapProject(point);
	}

}
