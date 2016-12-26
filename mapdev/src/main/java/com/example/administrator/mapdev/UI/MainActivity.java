package com.example.administrator.mapdev.UI;

import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.esri.android.map.MapView;
import com.esri.core.geometry.Point;
import com.example.administrator.mapdev.Action.AttributeEditorAction;
import com.example.administrator.mapdev.Action.DrawingAction;
import com.example.administrator.mapdev.Action.GpsRouteAction;
import com.example.administrator.mapdev.Action.PhotoCaptureAction;
import com.example.administrator.mapdev.Action.SurveyDataCaptureAction;
import com.example.administrator.mapdev.LayersManager;
import com.example.administrator.mapdev.MapApplication;
import com.example.administrator.mapdev.R;
import com.example.administrator.mapdev.SurveyDataExport;
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
        mApplication.setMainActivity(this);
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

    /**
     * 工具栏响应事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.photo_survey_gps:
                openCamera();
                break;
            case R.id.search_feature:
                mMapFragment.searchFeature();
                break;
            case R.id.gps_track_start:
                mMapFragment.startGpsRouteTrack();
                break;
            case R.id.gps_track_stop:
                mMapFragment.stopGpsRouteTrack();
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
            case R.id.map_exit:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.inflateMenu(R.menu.menu_tool);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
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
        mNavigationView.setNavigationItemSelectedListener(onNavigationItemSelected);
    }

    /**
     * 菜单面板工具
     */
    NavigationView.OnNavigationItemSelectedListener onNavigationItemSelected = new NavigationView.OnNavigationItemSelectedListener() {
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
                    openFileBrowser(RASTER_DATA_TYPE);
                    break;
                case R.id.import_vector:
                    openFileBrowser(SHP_DATA_TYPE);
                    break;
                case R.id.layer_manager:
                    //openLayersFragment();
                    openLayers2Fragment();
                    break;
                case R.id.survey_data:
                    //采集数据
                    if (mLayerManager.hasMapScene()) {
                        SurveyDataCaptureAction surveyDataCaptureAction=new SurveyDataCaptureAction();
                        startSupportActionMode(surveyDataCaptureAction);
                    } else
                        MapApplication.showMessage("必须创建地图或打开地图才能进行采集数据");
                    break;
                case R.id.survey_edit:
                    if (mLayerManager.hasMapScene()) {
                        //采集数据编辑
                        //setCurrentToolGroup(R.id.survey_edit_tool_group);
                        AttributeEditorAction attributeEditorAction = new AttributeEditorAction();
                        startSupportActionMode(attributeEditorAction);
                    } else
                        MapApplication.showMessage("必须创建地图或打开地图才能采集数据编辑");
                    break;
                case R.id.survey_draw:
                    if (mLayerManager.hasMapScene()) {
                        //地图标绘
                        //setCurrentToolGroup(R.id.survey_draw_tool_group);
                        DrawingAction drawingAction = new DrawingAction();
                        startSupportActionMode(drawingAction);
                    } else
                        MapApplication.showMessage("必须创建地图或打开地图才能地图标绘");
                    break;
                case R.id.survey_data_export:
                    //采集数据导出
                    exportSurveyData();
                    break;
                case R.id.photo_survey:
                    //采集现场照片
                    if (mLayerManager.hasMapScene()) {
                        //setCurrentToolGroup(R.id.photo_tool_group);
                        PhotoCaptureAction photoCaptureAction = new PhotoCaptureAction();
                        startSupportActionMode(photoCaptureAction);
                    } else
                        MapApplication.showMessage("必须创建地图或打开地图才能采集现场照片");
                    break;
                case R.id.photo_survey_import:
                    if (mMapFragment != null) {
                        mMapFragment.asyncLoadPhotoSurveyData();
                    }
                    break;
                case R.id.photo_survey_export:
                    exportPhotoSurveyData();
                    break;
                case R.id.gps_track:
                    //gps轨迹跟踪绘制
                    if (mLayerManager.hasMapScene()) {
                        //setCurrentToolGroup(R.id.gps_track_tool_group);
                        GpsRouteAction gpsRouteAction = new GpsRouteAction();
                        startSupportActionMode(gpsRouteAction);
                    }
                    else
                        MapApplication.showMessage("必须创建地图或打开地图才能GPS轨迹跟踪绘制");
                    break;
                case R.id.gps_track_export:
                    //gps轨迹跟踪绘制
                    if (mLayerManager.hasMapScene())
                        openRouteExportFragment();
                    else
                        MapApplication.showMessage("必须创建地图或打开地图才能使用GPS导出功能");
                    break;
                case R.id.gps_track_view:
                    if (mLayerManager.hasMapScene())
                        openRouteFragment();
                    else
                        MapApplication.showMessage("必须创建地图或打开地图才能使用GPS路径查看功能");
                    break;
                case R.id.map_view_setting:
                    openMapSettingFragment();
                    break;
                case R.id.help_about:
                    showAboutDialog();
                    break;
            }
            return true;
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //屏幕旋转处理事件
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

    static private int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 10;
    static private int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_POS=11;
    static private String currentCaptureImage;
    static private Location location ;
    /**
     * 采集照片（调用拍照Activity进行拍照)
     */
    public void openCamera() {
        java.util.Date now = new java.util.Date();
        currentCaptureImage = mApplication.getPhotoPath() + "/" + String.valueOf(now.getTime()) + ".jpg";
        File saveImageFile = new File(currentCaptureImage);
        Uri uri = Uri.fromFile(saveImageFile);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public void openCamera(Location point){
        java.util.Date now = new java.util.Date();
        currentCaptureImage = mApplication.getPhotoPath() + "/" + String.valueOf(now.getTime()) + ".jpg";
        File saveImageFile = new File(currentCaptureImage);
        Uri uri = Uri.fromFile(saveImageFile);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        //cameraIntent.putExtra("Location",point);
        location=point;
        startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_POS);
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
        else if( requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_POS ) {
            if (resultCode == RESULT_OK) {
                if (currentCaptureImage != null) {
                    //Location point = data.getParcelableExtra("Location");
                    mMapFragment.takePhotoAction(currentCaptureImage, location,"");
                }
            }
        }
    }

    //两秒内按返回键两次退出程序`
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

    private void openFileBrowser(int dataType) {
        if (!mLayerManager.hasMapScene()) {
            MapApplication.showMessage("必须创建地图或打开地图才能导入图层");
            return ;
        }
        String suffix = ".tiff;.tif;.img;";
        if (dataType == 1)
            suffix = ".shp;";
        FileBrowserFragment fileFragment = FileBrowserFragment.newInstance(dataType, suffix, mCurrentPath);
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, fileFragment).addToBackStack(null)
                .commit();
    }

    private String getDefaultStartDirectory() {
        mCurrentPath = mApplication.getDataPath();
        return mCurrentPath;
    }

    private void openLayersFragment() {
        LayersFragment layersFragment = LayersFragment.newInstance(mLayerManager, "", "");
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, layersFragment).addToBackStack(null)
                .commit();
    }

    private void openLayers2Fragment() {
        if (!mLayerManager.hasMapScene()) {
            MapApplication.showMessage("必须打开地图或创建地图才能使用图层管理");
            return ;
        }
        Layers2Fragment layers2Fragment = Layers2Fragment.newInstance(this.mLayerManager);
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, layers2Fragment).addToBackStack(null)
                .commit();
    }

    private void openRouteFragment() {
        GpsRouteViewFragment routeFragment = GpsRouteViewFragment.newInstance(mMapFragment.getGpsRouteTracker());
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, routeFragment).addToBackStack(null)
                .commit();
    }

    private void openRouteExportFragment() {
        GpsRouteExportFragment routeExportFragment = GpsRouteExportFragment.newInstance(mMapFragment.getGpsRouteTracker());
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, routeExportFragment).addToBackStack(null)
                .commit();
    }

    private void openCreateSceneFragment() {
        if(!MapApplication.instance().isLicenseVaild()) {
            MapApplication.showMessage("License已经到期，请使用正式版本");
            return;
        }
        MapSceneFragment mapSceneFragment = MapSceneFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, mapSceneFragment).addToBackStack(null)
                .commit();
    }

    private void openRecentSceneFragment() {
        if(!MapApplication.instance().isLicenseVaild()) {
            MapApplication.showMessage("License已经到期，请使用正式版本");
            return;
        }
        RecentSceneFragment recentSceneFragment = new RecentSceneFragment();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, recentSceneFragment).addToBackStack(null)
                .commit();
    }

    private void exportSurveyData() {
        //导出采集数据
        if (mLayerManager.hasMapScene()) {
            SurveyDataExport surveyDataExport = new SurveyDataExport(this);
            surveyDataExport.exportSurveyData();
        }else
            MapApplication.showMessage("必须创建地图或打开地图才能进行采集数据导出");
    }

    private void exportPhotoSurveyData() {
        //导出采集照片信息数据
        if (mLayerManager.hasMapScene()) {
            if( mLayerManager.getPhotoSurveyManager().exportPhotoSurveyData() ){
                MapApplication.showMessage("采集照片数导出成功");
            }
        }else
            MapApplication.showMessage("必须创建地图或打开地图才能进行采集数据导出");
    }

    private void openMapSettingFragment(){
        if (!mLayerManager.hasMapScene()) {
            MapApplication.showMessage("必须创建地图或打开地图");
            return ;
        }
        //地图设置窗口
        MapSettingFragment mapSettingFragment = MapSettingFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, mapSettingFragment).addToBackStack(null)
                .commit();
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
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("版权信息 1.1.26");
        builder.setMessage("江西省国土资源勘测规划院版权所有\n试用版本有效期至2017.01.05\n");
        builder.setPositiveButton("确定", null);
        builder.show();
    }
}
