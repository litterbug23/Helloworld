package com.example.administrator.mapdev.Action;

import android.location.Location;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Point;
import com.example.administrator.mapdev.LayersManager;
import com.example.administrator.mapdev.MapApplication;
import com.example.administrator.mapdev.R;
import com.example.administrator.mapdev.UI.MainActivity;

/**
 * 照片采集类
 * Created by Administrator on 2016/11/19.
 */
public class PhotoCaptureAction implements ActionMode.Callback {

    LayersManager layersManager;
    OnSingleTapListener defaultListener;
    MapView mapView;

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater menuInflater = mode.getMenuInflater();
        menuInflater.inflate(R.menu.menu_photo_tool,menu);
        layersManager = MapApplication.instance().getLayersManager();
        mapView = layersManager.getMapView();
        defaultListener = mapView.getOnSingleTapListener();
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.photo_survey_gps:   //照片位置使用GPS获得位置
                MainActivity mainActivity = (MainActivity)MapApplication.instance().getMainActivity();
                mainActivity.openCamera();
                break;
            case R.id.photo_survey_position: //照片位置在地图上取位置
                MapApplication.instance().showMessage("点击地图获取坐标后，开始照片采集");
                doCaptureByMapTouch();
                break;
            case R.id.photo_survey_delete:  //删除采集照片

                break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        reset();
    }

    private void reset() {
        //恢复操作，清理当前ActionMode退出时的所有操作
        mapView.setOnSingleTapListener(defaultListener);
    }

    /**
     * 手动地图上取点
     */
    private void doCaptureByMapTouch() {
        mapView.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(float v, float v1) {
                Point point = mapView.toMapPoint(v, v1);
                mapView.setOnSingleTapListener(defaultListener);
                Point wgsPoint = layersManager.mapProjectToWgs84(point);
                Location location= new Location("");
                location.setLongitude(wgsPoint.getX());
                location.setLatitude(wgsPoint.getY());
                MainActivity mainActivity = (MainActivity)MapApplication.instance().getMainActivity();
                mainActivity.openCamera(location);
            }
        });
    }


}
