package com.example.administrator.mapdev;

import com.esri.android.map.Layer;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2016/3/11.
 */
public class LayerItemData extends DataSupport {

	public static int UNKNOWN = 0;
	public static int POINT = 1;
	public static int POLYLINE = 2;
	public static int POLYGON = 3;
	public static int RASTER = 4;

	public static int GRAPHIC_LAYER = 0;
	public static int FEATURE_LAYER = 1;
	public static int KML_LAYER = 2;
	public static int RASTER_LAYER = 3;
	public static int WEB_LAYER = 4;
	public static int MAX_LAYER_TYPE = WEB_LAYER;

	public static String FEATURE_LAYER_TYPE = "矢量图层"; //"FeatureLayer";
	public static String GRAPHIC_LAYER_TYPE = "采集图层";//"GraphicLayer";
	public static String RASTER_LAYER_TYPE = "影像图层";//"RasterLayer";
	public static String KML_LAYER_TYPE = "KML图层";//"KmlLayer";
	public static String WEB_LAYER_TYPE = "Web图层";//"WebTileLayer";

	public static String[] layerTypeStrings = {
			GRAPHIC_LAYER_TYPE,
			FEATURE_LAYER_TYPE,
			KML_LAYER_TYPE,
			RASTER_LAYER_TYPE,
			WEB_LAYER_TYPE};

	@Column(unique = true)
	private int id;

	private int layerType; ///图层类型

	private int geometryType; //几何数据类型

	@Column(nullable = false)
	private String dataSource; //数据类型

	private int orderId; 	//图层在地图中的叠加次序

	private boolean visible=true;

	private String layerSymbol;		//图层样式

	private String labelFieldName;	//图层标注字段名称

	private MapScene mapScene;

	protected Layer layer;

	public Layer getLayer() { return layer; }

	public void setLayer(Layer layer) { this.layer=layer;}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public MapScene getMapScene() {
		return mapScene;
	}

	public void setMapScene(MapScene mapScene) {
		this.mapScene = mapScene;
	}

	public int getLayerType() {
		return layerType;
	}

	public void setLayerType(int layerType) {
		this.layerType = layerType;
	}

	public int getGeometryType() {
		return geometryType;
	}

	public void setGeometryType(int geometryType) {
		this.geometryType = geometryType;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	@Override
	public long getBaseObjId() {
		return super.getBaseObjId();
	}

	public String getLayerTypeString() {
		if (layerType <= MAX_LAYER_TYPE && layerType >= 0)
			return layerTypeStrings[layerType];
		else
			return "";
	}

	public boolean getVisible() {
		return isVisible();
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() { return visible;}

	public String getLayerSymbol() {
		return layerSymbol;
	}

	public void setLayerSymbol(String layerSymbol) {
		this.layerSymbol = layerSymbol;
	}

	public String getLabelFieldName() {
		return labelFieldName;
	}

	public void setLabelFieldName(String labelFieldName) {
		this.labelFieldName = labelFieldName;
	}
}
