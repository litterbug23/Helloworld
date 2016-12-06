package com.example.administrator.mapdev.UI;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.azeesoft.lib.colorpicker.ColorPickerDialog;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Point;
import com.example.administrator.mapdev.LayersManager;
import com.example.administrator.mapdev.MapApplication;
import com.example.administrator.mapdev.MapScene;
import com.example.administrator.mapdev.R;

/**

 */
public class MapSettingFragment extends Fragment {

    private MapScene mapScene;
    private MapView mapView;
    private LayersManager layersManager;
    /**
     * Use this factory method to create a new instance of
     *
     * @return A new instance of fragment MapSettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapSettingFragment newInstance() {
        MapSettingFragment fragment = new MapSettingFragment();
        return fragment;
    }

    public MapSettingFragment() {
        // Required empty public constructor
        layersManager = MapApplication.instance().getLayersManager();
        mapScene = layersManager.getCurrentScene();
        mapView = layersManager.getMapView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map_setting, container, false);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.recent_scene_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed();
            }
        });
        EditText txtLongitude = (EditText) view.findViewById(R.id.txt_longitude);
        EditText txtLatitude = (EditText) view.findViewById(R.id.txt_latitude);
        txtLongitude.setText(String.valueOf(mapScene.getCalibrationLong()));
        txtLatitude.setText(String.valueOf(mapScene.getCalibrationLat()));
        Button btnCalibration = (Button) view.findViewById(R.id.btn_touch_map);
        btnCalibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果GPS没有获得定位信息
                if( !mapView.getLocationDisplayManager().isStarted() ){
                    MapApplication.showMessage("GPS没有获得定位信息");
                    return ;
                }
                hideFragment();
                MapApplication.showMessage("长按地图获得校正点坐标");
                doCaptureByMapTouch();
            }
        });
        final Button btnColorPick = (Button) view.findViewById(R.id.btn_color_pick);
        btnColorPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog colorPickerDialog = ColorPickerDialog.createColorPickerDialog(MapSettingFragment.this.getContext());
                colorPickerDialog.setOnColorPickedListener(
                        new ColorPickerDialog.OnColorPickedListener() {
                            @Override
                            public void onColorPicked(int color, String hexVal) {
                                btnColorPick.setTextColor(color);
                            }
                        }
                );
                colorPickerDialog.show();
            }
        });
        EditText txtWktString = (EditText) view.findViewById(R.id.txt_wktString);
        txtWktString.setText(mapScene.getWktExt());
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void onButtonPressed() {
        View view = getView();
        EditText txtLongitude = (EditText) view.findViewById(R.id.txt_longitude);
        EditText txtLatitude = (EditText) view.findViewById(R.id.txt_latitude);
        //保存信息
        try {
            double longitude = Double.valueOf(txtLongitude.getText().toString());
            mapScene.setCalibrationLong(longitude);
        } catch (NumberFormatException e) {
            Log.d("MapSetting", e.getMessage());
        }
        try {
            double latitude = Double.valueOf(txtLatitude.getText().toString());
            mapScene.setCalibrationLat(latitude);
        } catch (NumberFormatException e) {
            Log.d("MapSetting", e.getMessage());
        }
        int id = mapScene.getId();
        mapScene.update(id);
        //弹出窗口
        getFragmentManager().popBackStack();
    }

    private void updateCalibration(double longitude,double latitude){
        View view = getView();
        EditText txtLongitude = (EditText) view.findViewById(R.id.txt_longitude);
        EditText txtLatitude = (EditText) view.findViewById(R.id.txt_latitude);
        txtLatitude.setText( String.valueOf(latitude) );
        txtLongitude.setText(String.valueOf(longitude));
        mapScene.setCalibrationLong(longitude);
        mapScene.setCalibrationLat(latitude);
        int id = mapScene.getId();
        mapScene.update(id);
    }

    /**
     * 恢复显示窗口
     */
    private void showFragment(){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.show(this);
        transaction.commit();
    }

    /**
     * 暂时隐藏窗口
     */
    private void hideFragment(){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.hide(this);
        transaction.commit();
    }

    /**
     * 手动地图上取点
     */
    private void doCaptureByMapTouch() {
        final OnLongPressListener longPressListener = mapView.getOnLongPressListener();
        mapView.setOnLongPressListener(new OnLongPressListener() {
            @Override
            public boolean onLongPress(float v, float v1) {
                Location location = mapView.getLocationDisplayManager().getLocation();
                if (location == null) {
                    return false;
                }
                Point gpsPoint = layersManager.wgs84ToMapProject(location);
                Point point = mapView.toMapPoint(v, v1);
                Point offset = new Point();
                offset.setX(point.getX() - gpsPoint.getX());
                offset.setY(point.getY() - gpsPoint.getY());
                updateCalibration(offset.getX(),offset.getY());
                showFragment();
                mapView.setOnLongPressListener(longPressListener);
                return true;
            }
        });
    }

}
