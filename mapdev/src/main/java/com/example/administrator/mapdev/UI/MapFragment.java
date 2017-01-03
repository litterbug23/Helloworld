package com.example.administrator.mapdev.UI;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.event.OnZoomListener;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.runtime.LicenseResult;
import com.esri.core.symbol.MarkerSymbol;
import com.example.administrator.mapdev.Compass;
import com.example.administrator.mapdev.GpsLocationService;
import com.example.administrator.mapdev.GpsRouteTracker;
import com.example.administrator.mapdev.LayersManager;
import com.example.administrator.mapdev.MapApplication;
import com.example.administrator.mapdev.PhotoSurveyManager;
import com.example.administrator.mapdev.R;
import com.example.administrator.mapdev.SensorService;
import com.example.administrator.mapdev.tools.AttributeEditorTool;
import com.example.administrator.mapdev.tools.AttributeTool;
import com.example.administrator.mapdev.tools.DrawTool;
import com.example.administrator.mapdev.tools.MeasureTool;
import com.example.administrator.mapdev.tools.ToolsManager;

import java.io.File;
import java.text.SimpleDateFormat;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {
    private MapView mMapView;
    private LayersManager mLayersManager;
    private MapApplication mApplication;
    private GpsLocationService mGpsLocationService;
    private GpsRouteTracker mGpsRouteTracker;
    private TextView mMapScaleUnit;
    private TextView mMapScale;
    private ImageView mMapScaleView;
    private ProgressBar mProgressBar;
    private Compass mCompass;
    private double mScaleWidthMeter;
    private double mScreenWidthMeter;   //屏幕宽度(单位米）
    private ToolsManager mToolsManager;
    private DrawTool mDrawTool;
    private MeasureTool mMeasureTool;
    private AttributeEditorTool mAttributeTool;

    private OnLongPressListener OnLongPress = new OnLongPressListener() {
        @Override
        public boolean onLongPress(float v, float v1) {
            if (mMapView.isLoaded()) {
                Point point = mMapView.toMapPoint(v, v1);
                if (!mMapView.getSpatialReference().isWGS84()) {
                    Point lonLat = (Point) GeometryEngine.project(point, mMapView.getSpatialReference(), SpatialReference.create(SpatialReference.WKID_WGS84));
                    Toast.makeText(getContext(), String.format("%f , %f\n%f , %f", point.getX(), point.getY(), lonLat.getX(), lonLat.getY()), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), String.format("%f , %f", point.getX(), point.getY()), Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        }
    };

    private OnStatusChangedListener onStatusChangedListener = new OnStatusChangedListener() {
        @Override
        public void onStatusChanged(Object o, STATUS status) {
            updateMapScaleView();
            if (o.equals(mMapView) && status == STATUS.INITIALIZED) {
                //初始化位置管理器
                LocationDisplayManager locationDisplayManager = mMapView.getLocationDisplayManager();
                locationDisplayManager.start();
                locationDisplayManager.setAllowNetworkLocation(true);
                //暂时关闭位置服务显示定位图标功能
                locationDisplayManager.setShowLocation(false);
                locationDisplayManager.setAccuracyCircleOn(false);
                locationDisplayManager.setShowPings(false);
                locationDisplayManager.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
                locationDisplayManager.setLocationListener(OnLocationListener);
                //初始化
                mGpsRouteTracker = new GpsRouteTracker(mLayersManager);
                mToolsManager = new ToolsManager(mLayersManager);
                mDrawTool = new DrawTool(mLayersManager);
                mMeasureTool = new MeasureTool(mLayersManager);
                mAttributeTool = new AttributeEditorTool(mLayersManager);
                mToolsManager.registerTool(DrawTool.class, mDrawTool);
                mToolsManager.registerTool(MeasureTool.class, mMeasureTool);
                mToolsManager.registerTool(AttributeEditorTool.class, mAttributeTool);

                mMapView.setOnSingleTapListener(new OnSingleTapListener() {
                    public void onSingleTap(float x, float y) {
                        // TODO Auto-generated method stub
                        GraphicsLayer graphicsLayer = mLayersManager.getUserDrawerLayer();
                        int[] graphicIDs = graphicsLayer.getGraphicIDs(x, y, 25);
                        if (graphicIDs != null && graphicIDs.length > 0) {
                            LayoutInflater inflater = LayoutInflater.from(getContext());
                            View view = inflater.inflate(R.layout.calloutdisplay, null);
                            Button exit = (Button) view.findViewById(R.id.exit_button);
                            exit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Callout callout = mMapView.getCallout();
                                    callout.hide();
                                }
                            });
                            Graphic gr = graphicsLayer.getGraphic(graphicIDs[0]);
                            if (gr.getGeometry().getType() == Geometry.Type.POINT) {
                                Point location = (Point) gr.getGeometry();
                                try {
                                    String image = (String) gr.getAttributeValue("thumbnail");
                                    double longitude = (double) gr.getAttributeValue("longitude");
                                    double latitude = (double) gr.getAttributeValue("latitude");
                                    double azimuth = (double) gr.getAttributeValue("azimuth");
                                    String comment = (String) gr.getAttributeValue("comment");
                                    long date = (long) gr.getAttributeValue("date");
                                    TextView locationView = (TextView) view.findViewById(R.id.location_content);
                                    locationView.setText(String.format("%.4f %.4f", longitude, latitude));
                                    TextView localLocView = (TextView) view.findViewById(R.id.local_location_content);
                                    localLocView.setText(String.format("%.1f %.1f", location.getX(), location.getY()));
                                    TextView azimuthView = (TextView) view.findViewById(R.id.photo_azimuth_content);
                                    azimuthView.setText(SensorService.getAzimuthString(azimuth));
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日hh:mm:ss");
                                    TextView dateView = (TextView) view.findViewById(R.id.photo_date_content);
                                    dateView.setText(dateFormat.format(date));
                                    TextView commentView = (TextView) view.findViewById(R.id.comment_content);
                                    commentView.setText(comment);
                                    if (image != null) {
                                        File imgFile = new File(image);
                                        if (imgFile.exists()) {
                                            //Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                            ImageView imageView = (ImageView) view.findViewById(R.id.photo_content);
                                            imageView.setImageURI(Uri.fromFile(imgFile));
                                            //imageView.setImageBitmap(bitmap);
                                        }
                                    }
                                } catch (NullPointerException e) {
                                    //ignore exception
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                Callout callout = mMapView.getCallout();
                                callout.setStyle(R.xml.calloutstyle);
                                callout.setOffset(0, -15);
                                callout.show(location, view);
                            }
                        }
                        Log.v("mapdev", "OnSingleTapLinstener is running !");
                    }
                });
            }
        }
    };

    private LocationListener OnLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(mLayersManager !=null && mLayersManager.getLocaionDrawerLayer()!=null){
                GraphicsLayer graphicsLayer = mLayersManager.getLocaionDrawerLayer();
                try {
                    LocationDisplayManager locationDisplayManager = mMapView.getLocationDisplayManager();
                    MarkerSymbol markSymbol = locationDisplayManager.getLocationAcquiringSymbol();
                    Location correctLocation = mLayersManager.getCalibrateLocation(location);
                    Point point = mLayersManager.wgs84ToMapProject(correctLocation);
                    graphicsLayer.removeAll();
                    Graphic graphic = new Graphic(point,markSymbol);
                    graphicsLayer.addGraphic(graphic);
                }catch (Exception e){
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private OnZoomListener OnZoomChange = new OnZoomListener() {
        @Override
        public void preAction(float v, float v1, double v2) {
        }

        @Override
        public void postAction(float v, float v1, double v2) {
            //float pivotX, float pivotY, double facto
            //更新地图比例尺(在地图显示比例尺）
            updateMapScaleView();
        }
    };

    private View.OnClickListener OnGpsClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LocationDisplayManager locationDisplayManager = mMapView.getLocationDisplayManager();
            locationDisplayManager.setShowLocation(false);
            locationDisplayManager.setShowPings(false);
            Location location = locationDisplayManager.getLocation();
            if (location != null)
                centerAt(location);
            else {
                if (!locationDisplayManager.isStarted())
                    locationDisplayManager.start();
            }
//			if (mGpsLocationService.isLocationAvailable()) {
//				location = mGpsLocationService.getCurrentBestLocation();
//				centerAt(location);
//			} else {
//				//如果gps服务没有启动，尝试启动gps服务
//				mGpsLocationService.resume();
//			}
        }
    };


    private View.OnClickListener OnZoomInClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mMapView.isLoaded()) {
                mMapView.zoomin(true);
            }
        }
    };

    private View.OnClickListener OnZoomOutClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mMapView.isLoaded()) {
                mMapView.zoomout(true);
            }
        }
    };

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = MapApplication.instance();
        mGpsLocationService = mApplication.getGpsLocationService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        mMapView = (MapView) view.findViewById(R.id.map);
        // Set the MapView to allow the user to rotate the map when as part of a pinch gesture.
        //mMapView.setAllowRotationByPinch(true);
        // Enabled wrap around map.
        mMapView.enableWrapAround(true);
        // Create the Compass custom view, and add it onto the MapView.
        mCompass = new Compass(getContext(), null, mMapView);
        mMapView.addView(mCompass);
        mMapView.setOnZoomListener(OnZoomChange);
        mMapView.setOnLongPressListener(OnLongPress);
        ImageButton gpsButton = (ImageButton) view.findViewById(R.id.gps_position);
        gpsButton.setOnClickListener(OnGpsClick);
        ImageButton zoomInButton = (ImageButton) view.findViewById(R.id.map_zoom_in);
        zoomInButton.setOnClickListener(OnZoomInClick);
        ImageButton zoomOutButton = (ImageButton) view.findViewById(R.id.map_zoom_out);
        zoomOutButton.setOnClickListener(OnZoomOutClick);
        //地图比例尺
        mMapScaleUnit = (TextView) view.findViewById(R.id.map_scale_text);
        mMapScale = (TextView) view.findViewById(R.id.map_scale);
        mMapScaleView = (ImageView) view.findViewById(R.id.scale_view);
        ViewTreeObserver vto = mMapScaleView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mMapScaleView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                ViewGroup.LayoutParams layoutParams = mMapScaleView.getLayoutParams();
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int widthPixel = mMapScaleView.getWidth(); //pixel
                mScaleWidthMeter = LayersManager.METER_PER_INCH * widthPixel / (double) metrics.densityDpi;
            }
        });
        initMapView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public LayersManager getLayersManager() {
        return mLayersManager;
    }

    private void initMapView() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        //1dpi= (n)pixel/inch
        //1dp = dpi/(160pixel/inch) = 1density/160
        //density = 160;240;  ( n Pixel/inch)
        //densityDpi = 1dp
        // px = density * dp
        //获得屏幕宽度
        mScreenWidthMeter = LayersManager.METER_PER_INCH * metrics.widthPixels / (double) metrics.densityDpi;
        //删除所有图层
        mMapView.removeAll();
        mMapView.setMaxScale(1.0);  //图上一米实际一米（最已经是最大的放大系数）
        //地球周长40076km
        double minScale = 40076000.0 / mScreenWidthMeter;
        LicenseResult licenseResult = ArcGISRuntime.setClientId("4YQLgHoXtvwXiBTu");
        //ArcGISRuntime.License.setLicense()
        mMapView.setMinScale(minScale);
//		SpatialReference spatialRef = SpatialReference.create(SpatialReference.WKID_WGS84_WEB_MERCATOR);
//		SpatialReference wgs84 = SpatialReference.create(SpatialReference.WKID_WGS84);
//		Envelope worldExtent = new Envelope(-180, -90, 180, 90);
//		Envelope fullExtent = (Envelope) GeometryEngine.project(worldExtent, wgs84, spatialRef);
//		GraphicsLayer graphLayer = new GraphicsLayer(spatialRef, fullExtent);
//		mMapView.addLayer(graphLayer);
        mLayersManager = new LayersManager(mMapView);
        mLayersManager.setScreenWidthMeter(mScreenWidthMeter);
        //mLayersManager.loadMapLayers();
        mLayersManager.setOnStatusChangedListener(onStatusChangedListener);
        //MapView的缺省地图范围和坐标参考系是根据BaseLayer来决定
//		mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
//			@Override
//			public void onStatusChanged(Object o, STATUS status) {
//				//setMapViewOptionsByBaseLayer();
//				if (o instanceof MapView )
//					mLayerManager.loadMapLayers();
//			}
//		});
    }

    public void takePhotoAction(String imagePath, String comment) {
        if (mMapView.isLoaded())
            mLayersManager.getPhotoSurveyManager().takePhotoAction(imagePath, comment);
        else
            Toast.makeText(getContext(), "当前没有地图数据，不能采集图片", Toast.LENGTH_SHORT).show();
    }

    public void takePhotoAction(String imagePath, Location location, String comment) {
        if (mMapView.isLoaded())
            mLayersManager.getPhotoSurveyManager().takePhotoAction(imagePath, location, comment);
        else
            Toast.makeText(getContext(), "当前没有地图数据，不能采集图片", Toast.LENGTH_SHORT).show();
    }


    /**
     * 将GPS坐标转换为地图投影坐标
     *
     * @param location
     * @return
     */
    private Point locationToPoint(Location location) {
        Point position = new Point(location.getLongitude(), location.getLatitude(), location.getAltitude());
        if (!LayersManager.wgs84.equals(mMapView.getSpatialReference()))
            position = (Point) GeometryEngine.project(position, LayersManager.wgs84, mMapView.getSpatialReference());
        return position;
    }

    /**
     * 定位地图位置到制定坐标
     *
     * @param location
     */
    private void centerAt(Location location) {
        //location = mLayersManager.getCalibrateLocation(location);
        Point position = locationToPoint(location);
        //中心点定位坐标校正
        double x = position.getX() + mLayersManager.getCurrentScene().getCalibrationLong();
        double y = position.getY() + mLayersManager.getCurrentScene().getCalibrationLat();
        position.setX(x);
        position.setY(y);
        mMapView.centerAt(position, true);
        updateMapScaleView();
    }

    /**
     * 更新地图比例尺显示
     */
    private void updateMapScaleView() {
        double mapScale = mMapView.getScale();
        double geoDistance = mScaleWidthMeter * mapScale;
        String mapScaleUnit;
        if (geoDistance < 1000) {
            mapScaleUnit = String.format("%.1f 米", geoDistance);
        } else if (geoDistance >= 1000 && geoDistance < 10000) {
            mapScaleUnit = String.format("%.2f 千米", geoDistance / 1000);
        } else if (geoDistance >= 10000 && geoDistance < 100000) {
            mapScaleUnit = String.format("%.1f 千米", geoDistance / 1000);
        } else {
            mapScaleUnit = String.format("%.0f 千米", geoDistance / 1000);
        }
        mMapScaleUnit.setText(mapScaleUnit);
        String mapScaleTex = String.format("1:%d", (int) (mapScale < 1 ? 1 : mapScale));
        mMapScale.setText(mapScaleTex);
    }

    /**
     * 设置长度测量
     */
    public void measureLength() {
        if (mMapView.isLoaded()) {
            mToolsManager.setCurrentTool(MeasureTool.class);
            mToolsManager.getCurrentTool().activate(MeasureTool.MEASURE_LENGTH);
        } else {
            Toast.makeText(getContext(), "当前没有数据图层加载，不能进行测量", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 设置面积测量
     */
    public void measureArea() {
        if (mMapView.isLoaded()) {
            mToolsManager.setCurrentTool(MeasureTool.class);
            mToolsManager.getCurrentTool().activate(MeasureTool.MEASURE_AREA);
        } else {
            Toast.makeText(getContext(), "当前没有数据图层加载，不能进行测量", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 绘制要素对象
     *
     * @param drawType DrawTool.POLYGON DrawTool.POLYLINE DrawTool.POINT DrawTool.FREE_HAND_POLYLINE
     */
    public void drawFeature(int drawType) {
//        if (mDrawTool != null) {
//            mDrawTool.activate(drawType);
//        }
        if (mMapView.isLoaded()) {
            mToolsManager.setCurrentTool(DrawTool.class);
            mToolsManager.getCurrentTool().activate(drawType);
        } else {
            Toast.makeText(getContext(), "当前没有数据图层加载，不能进行测量", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 同步加载所有外业采集照片数据(已经不再使用)
     */
    @Deprecated
    public void loadPhotoSurveyData() {
        if (mMapView.isLoaded()) {
            mProgressBar.setVisibility(View.VISIBLE);
            mLayersManager.getPhotoSurveyManager().loadPhotoSurveyData(mProgressBar);
            mProgressBar.setVisibility(View.INVISIBLE);
        } else {
            Toast.makeText(getContext(), "当前没有数据图层加载，不能加载采集点数据", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 异步加载所有外业采集照片数据
     */
    public void asyncLoadPhotoSurveyData() {
        if (mMapView.isLoaded()) {
            mLayersManager.getPhotoSurveyManager().asyncLoadPhotoSurveyData(mProgressBar);
        } else {
            Toast.makeText(getContext(), "当前没有数据图层加载，不能加载采集点数据", Toast.LENGTH_SHORT).show();
        }
    }

    public GpsRouteTracker getGpsRouteTracker() {
        return mGpsRouteTracker;
    }

    public void startGpsRouteTrack() {
        if (mMapView.isLoaded()) {
            mGpsRouteTracker.startRouteTracker();
        }
    }

    public void stopGpsRouteTrack() {
        if (mMapView.isLoaded()) {
            mGpsRouteTracker.stopRouteTracker();
        }
    }

    public void loadAllGpsRouteTrack() {
        if (mMapView.isLoaded()) {
            mGpsRouteTracker.loadAllRouteTracker();
        }
    }

    public void searchFeature() {
        if (mMapView.isLoaded()) {
            mToolsManager.setCurrentTool(AttributeEditorTool.class);
            mToolsManager.getCurrentTool().activate(AttributeTool.ATTRIBUTE_SHOW);
        } else {
            Toast.makeText(getContext(), "当前没有数据图层加载，不能进行测量", Toast.LENGTH_SHORT).show();
        }
    }

    public void resetTool() {
        if (mMapView.isLoaded()) {
            mToolsManager.reset();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
