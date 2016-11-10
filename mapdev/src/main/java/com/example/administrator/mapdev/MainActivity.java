package com.example.administrator.mapdev;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.runtime.LicenseResult;
import com.example.administrator.mapdev.tools.DrawTool;

import java.io.File;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity implements
        FileBrowserFragment.OnFragmentInteractionListener,
        LayersFragment.OnFragmentInteractionListener {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;
    private MapView mMapView;
    private MapFragment mMapFragment;
    private double mScreenWidthMeter;   //屏幕宽度(单位米）
    private LayersManager mLayerManager;
    private MapApplication mApplication;
    //当前路径
    private String mCurrentPath;
    //缺省存储路径（MapDev)
    private String mDefaultStoragePath;

    static final private int RASTER_DATA_TYPE = 0;
    static final private int SHP_DATA_TYPE = 1;
    static final private int GDB_DATA_TYPE = 2;
    static final private int KML_DATA_TYPE = 3;
    static final private double METER_PER_INCH = 0.0254;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mApplication = (MapApplication) getApplication();
        mMapView = (MapView) findViewById(R.id.map);
        mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mLayerManager = mMapFragment.getLayersManager();
        mApplication.setLayersManager(mLayerManager);
        getDefaultStartDirectory();
        initToolbar();
        initDrawerLayout();
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "onMenuOpened...unable to set icons for overflow menu", e);
                }
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tool, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.photo_survey:
                openCamera();
                break;
            case R.id.search_feature:
                mMapFragment.searchFeature();
                break;
            case R.id.add_label:
                mMapFragment.drawFeature(DrawTool.POINT);
                break;
            case R.id.add_polyline:
                mMapFragment.drawFeature(DrawTool.POLYLINE);
                break;
            case R.id.add_polygon:
                mMapFragment.drawFeature(DrawTool.POLYGON);
                break;
            case R.id.measure_distance:
                mMapFragment.measureLength();
                break;
            case R.id.measure_area:
                mMapFragment.measureArea();
                break;
            case R.id.reset_tool:
                mMapFragment.resetTool();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_tool);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void changeToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.getMenu().setGroupVisible(R.id.draw_edit_tool_group, true);
        //toolbar.inflateMenu(R.menu.menu_edit_tool);
    }

    /**
     * 初始化侧滑栏
     */
    private void initDrawerLayout() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mNavigationView = (NavigationView) findViewById(R.id.navigation);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                int menuId = menuItem.getItemId();
                switch (menuId) {
                    case R.id.create_map_scene:
                        openCreateSceneFragment();
                        break;
                    case R.id.open_recent_map:
                        openRecentSceneFragment();
                        break;
                    case R.id.import_raster:
                        if (mLayerManager.hasMapScene())
                            openFileBrowser(RASTER_DATA_TYPE);
                        else
                            MapApplication.showMessage("必须创建地图或打开地图才能导入图层");
                        break;
                    case R.id.import_vector:
                        if (mLayerManager.hasMapScene())
                            openFileBrowser(SHP_DATA_TYPE);
                        else
                            MapApplication.showMessage("必须创建地图或打开地图才能导入图层");
                        break;
                    case R.id.layer_manager:
                        //openLayersFragment();
                        openLayers2Fragment();
                        break;
                    case R.id.photo_survey_import:
                        if (mMapFragment != null) {
                            mMapFragment.asyncLoadPhotoSurveyData();
                        }
                        break;
                    case R.id.photo_survey_export:
                        break;
                    case R.id.gps_track_start:
                        mMapFragment.startGpsRouteTrack();
                        break;
                    case R.id.gps_track_stop:
                        mMapFragment.stopGpsRouteTrack();
                        break;
                    case R.id.gps_track_export:
                        openRouteExportFragment();
                        break;
                    case R.id.gps_track_view:
                        openRouteFragment();
                    case R.id.mapview_setting:
                        break;
                    case R.id.help_about:
                        showAboutDialog();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //initToolbar();
            //竖屏
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //initToolbar();
            //横屏
            //1 inch = 0.0254 m
            mScreenWidthMeter = METER_PER_INCH * newConfig.screenWidthDp / (double) newConfig.densityDpi;
        }
    }

    @Deprecated
    private void initMapView() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //1dpi= (n)pixel/inch
        //1dp = dpi/(160pixel/inch) = 1density/160
        //density = 160;240;  ( n Pixel/inch)
        //densityDpi = 1dp
        // px = density * dp
        //获得屏幕宽度
        mScreenWidthMeter = METER_PER_INCH * metrics.widthPixels / (double) metrics.densityDpi;
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
        mLayerManager = new LayersManager(mMapView);
        mLayerManager.setScreenWidthMeter(mScreenWidthMeter);
        //mLayerManager.loadMapLayers();
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

    static private int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 10;
    static private String currentCaptureImage;

    private void openCamera() {
        java.util.Date now = new java.util.Date();
        currentCaptureImage = mApplication.getPhotoPath() + "/" + String.valueOf(now.getTime()) + ".jpg";
        File saveImageFile = new File(currentCaptureImage);
        Uri uri = Uri.fromFile(saveImageFile);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (currentCaptureImage != null) {
                    mMapFragment.takePhotoAction(currentCaptureImage, "");
                }
            }
        }
    }

    //两秒内按返回键两次退出程序
    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                    System.exit(0);
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Deprecated
    private void setMapViewOptionsByBaseLayer() {
        //地图第一次被初始化被调用此函数
        if (!mMapView.isLoaded())
            return;
        SpatialReference mapSpatialRef = mMapView.getSpatialReference();
        //GraphicLayer用来绘制临时数据（比如测量等）
        GraphicsLayer graphLayer = new GraphicsLayer(mapSpatialRef, mMapView.getMaxExtent());
        mMapView.addLayer(graphLayer);
        mMapView.getLocationDisplayManager();
    }

    private void openFileBrowser(int dataType) {
        String suffix = ".tiff;.tif;.img;";
        if (dataType == 1)
            suffix = ".shp;";
        FileBrowserFragment fileFragment = FileBrowserFragment.newInstance(dataType, suffix, mCurrentPath);
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, fileFragment).addToBackStack(null)
                .commit();
    }

    private String getDefaultStartDirectory() {
        File path;
        // No or invalid directory supplied in intent
        if (Environment.getExternalStorageDirectory().isDirectory()
                && Environment.getExternalStorageDirectory().canRead()) {
            path = Environment.getExternalStorageDirectory();
        } else {
            path = new File("/");
        }
        mCurrentPath = path.toString();
        return mCurrentPath;
    }

    private void openLayersFragment() {
        LayersFragment layersFragment = LayersFragment.newInstance(mLayerManager, "", "");
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, layersFragment).addToBackStack(null)
                .commit();
    }

    private void openRouteFragment() {
        RouteFragment routeFragment = RouteFragment.newInstance(mMapFragment.getGpsRouteTracker());
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, routeFragment).addToBackStack(null)
                .commit();
    }

    private void openRouteExportFragment() {
        RouteExportFragment routeExportFragment = RouteExportFragment.newInstance(mMapFragment.getGpsRouteTracker());
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, routeExportFragment).addToBackStack(null)
                .commit();
    }

    private void openCreateSceneFragment() {
        SceneFragment sceneFragment = SceneFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, sceneFragment).addToBackStack(null)
                .commit();
    }

    private void openRecentSceneFragment() {
        RecentSceneFragment recentSceneFragment = new RecentSceneFragment();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, recentSceneFragment).addToBackStack(null)
                .commit();
    }

    private void openLayers2Fragment() {
        Layers2Fragment layers2Fragment = Layers2Fragment.newInstance(this.mLayerManager);
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, layers2Fragment).addToBackStack(null)
                .commit();
        //changeToolbar();
    }

    @Override
    public void onFragmentInteraction(int fragment_id, int action_type, int data_type, @Nullable Bundle arguments) {
        if (action_type == FileBrowserFragment.ACTION_UP) {
            if (arguments != null) {
                mCurrentPath = arguments.getString("path");
                File currentPath = new File(mCurrentPath);
                if (currentPath.isFile()) {
                    mCurrentPath = currentPath.getParent();
                }
            }
            getSupportFragmentManager().popBackStack();
        } else if (action_type == FileBrowserFragment.ACTION_FILE) {
            mCurrentPath = arguments.getString("path");
            File currentPath = new File(mCurrentPath);
            if (data_type == 0)
                //loadRasterLayer(mCurrentPath);
                mLayerManager.loadRasterLayer(mCurrentPath);
            else if (data_type == 1)
                //loadVectorLayer(mCurrentPath);
                mLayerManager.loadVectorLayer(mCurrentPath);
            mCurrentPath = currentPath.getParent();
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    public void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("版权信息");
        builder.setMessage("江西省国土资源勘测规划院版权所有");
        builder.setPositiveButton("确定", null);
        builder.show();
    }
}
