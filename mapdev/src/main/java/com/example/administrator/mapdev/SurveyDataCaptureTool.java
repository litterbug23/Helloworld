package com.example.administrator.mapdev;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.AlertDialog;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/14.
 * 采集数据工具
 */
public class SurveyDataCaptureTool {
    //GPS取点,手动取点,编辑撤销，结束保存
    private SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(Color.BLUE, 10, SimpleMarkerSymbol.STYLE.DIAMOND);
    private SimpleLineSymbol lineSymbol = new SimpleLineSymbol(Color.YELLOW, 3);
    private SimpleFillSymbol fillSymbol = new SimpleFillSymbol(Color.argb(172, 61, 237, 16));
    private LocationDisplayManager locationDisplayManager;
    private LayersManager layersManager;
    private List<Point> undoHistories = new ArrayList<>();  //撤销
    private List<Point> redoHistories = new ArrayList<>();  //重做
    private Graphic polylineGraphic = null;
    private Polyline polyline = null;
    private MultiPoint multiPoint = null;
    private Graphic pointsGraphic = null;
    private GraphicsLayer graphicsLayer;
    private int pointsGID = 0;
    private int polylineGID = 0;
    MapView mapView;
    OnSingleTapListener defaultListener;

    public SurveyDataCaptureTool() {
        fillSymbol.setOutline(new SimpleLineSymbol(Color.argb(255, 73, 137, 243), 2));
        layersManager = MapApplication.instance().getLayersManager();
        locationDisplayManager = layersManager.getMapView().getLocationDisplayManager();
        mapView = layersManager.getMapView();
        defaultListener = mapView.getOnSingleTapListener();
    }

    public void initSurveyDataCaptureTool() {
        undoHistories.clear();
        redoHistories.clear();
        polyline = new Polyline();
        multiPoint = new MultiPoint();
        polylineGraphic = new Graphic(polyline, lineSymbol);
        pointsGraphic = new Graphic(multiPoint, markerSymbol);
        graphicsLayer = layersManager.getDrawerLayer();
        if (polylineGID != 0)
            graphicsLayer.removeGraphic(polylineGID);
        polylineGID = graphicsLayer.addGraphic(polylineGraphic);
        if (pointsGID != 0)
            graphicsLayer.removeGraphic(pointsGID);
        pointsGID = graphicsLayer.addGraphic(pointsGraphic);
    }

    public void unInitSurveyDataCaptureTool() {
        clear();
        mapView.setOnSingleTapListener(defaultListener);
    }

    private Location getLocation() {
        //return gpsLocationService.getCurrentBestLocation();
        Location location = locationDisplayManager.getLocation();
        if (location == null) {
            if (!locationDisplayManager.isStarted())
                locationDisplayManager.start();
        }
        return location;
    }


    private boolean isValidate() {
        graphicsLayer = layersManager.getDrawerLayer();
        if (graphicsLayer == null)
            return false;
        if (polyline == null)
            polyline = new Polyline();
        if (multiPoint == null)
            multiPoint = new MultiPoint();
        if (polylineGraphic == null) {
            polylineGraphic = new Graphic(polyline, lineSymbol);
            polylineGID = graphicsLayer.addGraphic(polylineGraphic);
        }
        if (pointsGraphic == null) {
            pointsGraphic = new Graphic(multiPoint, markerSymbol);
            pointsGID = graphicsLayer.addGraphic(pointsGraphic);
        }
        return true;
    }

    private void addPoint(Point point) {
        if (!isValidate())
            return;
        undoHistories.add(point);
        multiPoint.add(point);
        if (undoHistories.size() == 1)
            polyline.startPath(point);
        else
            polyline.lineTo(point);
        graphicsLayer.updateGraphic(pointsGID, multiPoint);
        graphicsLayer.updateGraphic(polylineGID, polyline);
    }

    private void removePoint(Point point) {
        if (!isValidate())
            return;
        redoHistories.add(point);
        int size = multiPoint.getPointCount();
        multiPoint.removePoint(size - 1);
        if (undoHistories.size() >= 1) {
            int index = polyline.getPointCount();
            polyline.removePoint(index - 1);
        }
        graphicsLayer.updateGraphic(pointsGID, multiPoint);
        graphicsLayer.updateGraphic(polylineGID, polyline);
    }

    /**
     * 从GPS取当前位置
     */
    public void doCaptureGPSLocation() {
        Location location = getLocation();
        if (location == null) {
            MapApplication.showMessage("未获得GPS或者网络定位信号");
            return;
        }
        Point point = layersManager.wgs84ToMapProject(location);
        //校正位置坐标
        double x = point.getX() + layersManager.getCurrentScene().getCalibrationLong();
        double y = point.getY() + layersManager.getCurrentScene().getCalibrationLat();
        point.setX(x);
        point.setY(y);
        addPoint(point);
    }

    /**
     * 手动地图上取点
     */
    public void doCaptureByMapTouch() {
        mapView.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(float v, float v1) {
                Point point = mapView.toMapPoint(v, v1);
                addPoint(point);
                mapView.setOnSingleTapListener(defaultListener);
            }
        });
    }

    private int dataType = 0;

    /**
     * 导出数据到
     *
     * @param datatype 0 表示多边形 1表示线段
     */
    private void saveSurveyData(int datatype) {
        boolean isPolygon = datatype == 0 ? true : false;
        if (isPolygon) {
            SurveyDataLayer surveyDataLayer = layersManager.getSurveyPolygonLayer();
            Polygon polygon = new Polygon();
            polygon.startPath(undoHistories.get(0));
            for (int i = 1; i < undoHistories.size(); i++) {
                polygon.lineTo(undoHistories.get(i));
            }
            polygon.lineTo(undoHistories.get(0));
            Graphic graphic = new Graphic(polygon, surveyDataLayer.getDefaultSymbol());
            surveyDataLayer.addGraphic(graphic);
        } else {
            SurveyDataLayer surveyDataLayer = layersManager.getSurveyPolylineLayer();
            Polyline polyline = new Polyline();
            polyline.startPath(undoHistories.get(0));
            for (int i = 1; i < undoHistories.size(); i++) {
                polyline.lineTo(undoHistories.get(i));
            }
            Graphic graphic = new Graphic(polyline, surveyDataLayer.getDefaultSymbol());
            surveyDataLayer.addGraphic(graphic);
        }
        clear();
    }

    public void doComplete() {
        if (undoHistories.size() < 2) {
            MapApplication.showMessage("获得的顶点数不足3个，至少需要获得三个顶点");
            return;
        }
        Activity mainActivity = MapApplication.instance().getMainActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("选择图斑图形类别");
        builder.setCancelable(false);
        builder.setSingleChoiceItems(new String[]{"面状图斑", "线状图斑"}, dataType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dataType = which;
            }
        });
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveSurveyData(dataType);
            }
        });
        builder.show();
    }

    public void clear() {
        undoHistories.clear();
        redoHistories.clear();
        graphicsLayer.removeGraphic(polylineGID);
        graphicsLayer.removeGraphic(pointsGID);
        polylineGraphic = null;
        pointsGraphic = null;
        polyline = null;
        multiPoint = null;
    }

    public boolean canRedo() {
        if (redoHistories.size() > 0)
            return true;
        else
            return false;
    }

    /**
     * 重做
     */
    public void redo() {
        if (!canRedo())
            return;
        Point point = redoHistories.remove(redoHistories.size() - 1);
        addPoint(point);
    }

    public boolean canUndo() {
        if (undoHistories.size() > 0)
            return true;
        else
            return false;
    }

    /**
     * 撤销
     */
    public void undo() {
        if (!canUndo())
            return;
        Point point = undoHistories.remove(undoHistories.size() - 1);
        removePoint(point);
    }


}
