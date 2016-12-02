package com.example.administrator.mapdev.tools;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.core.geometry.AreaUnit;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.example.administrator.mapdev.LayersManager;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/3/29.
 */
public class MeasureTool extends BaseTool {

    private SimpleLineSymbol lineSymbol;
    private SimpleMarkerSymbol markerSymbol;
    private SimpleFillSymbol fillSymbol;
    private Geometry.Type geoType = null;//用于判定当前选择的几何图形类型
    private Point ptStart = null;//起点
    private Point ptPrevious = null;//上一个点
    private ArrayList<Point> points = null;//记录全部点
    private Polygon tempPolygon = null;//记录绘制过程中的多边形
    private GraphicsLayer drawLayer;
    private boolean bIsMeasureLength;
    private int fontSize = 16;
    private int fontColor = Color.YELLOW;
    private String fontFamily = "DroidSansFallback.ttf";
    private int mUid = 0;
    public static final int MEASURE_LENGTH = 1;
    public static final int MEASURE_AREA = 2;

    public MeasureTool(LayersManager layersManager) {
        super(layersManager);
        mapOnTouchListener = new DrawTouchListener(this.mapView.getContext(), mapView);
        drawLayer = layersManager.getDrawerLayer();
        points = new ArrayList<>();
        lineSymbol = new SimpleLineSymbol(Color.RED, 3, SimpleLineSymbol.STYLE.DOT);
        markerSymbol = new SimpleMarkerSymbol(Color.BLUE, 8, SimpleMarkerSymbol.STYLE.CIRCLE);
        fillSymbol = new SimpleFillSymbol(Color.argb(150, 255, 0, 0), SimpleFillSymbol.STYLE.SOLID);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        // 其他清理工作
        ptStart = null;
        ptPrevious = null;
        points.clear();
        tempPolygon = null;
    }

    @Override
    public void activate(int toolType) {
        super.activate(toolType);
        if (toolType == MEASURE_LENGTH) {
            setIsMeasureLength(true);
        } else if (toolType == MEASURE_AREA) {
            setIsMeasureLength(false);
        }
    }

    /**
     * 设置是否测量长度
     *
     * @param bool
     */
    private void setIsMeasureLength(boolean bool) {
        bIsMeasureLength = bool;
        if (bIsMeasureLength)
            this.geoType = Geometry.Type.POLYLINE;
        else
            this.geoType = Geometry.Type.POLYGON;
    }

//    // 根据用户选择设置当前绘制的几何图形类型
//    public void setType(String geometryType) {
//        if (geometryType.equalsIgnoreCase("Point"))
//            this.geoType = Geometry.Type.POINT;
//        else if (geometryType.equalsIgnoreCase("Polyline"))
//            this.geoType = Geometry.Type.POLYLINE;
//        else if (geometryType.equalsIgnoreCase("Polygon"))
//            this.geoType = Geometry.Type.POLYGON;
//    }
//
//    public Geometry.Type getType() {
//        return this.geoType;
//    }

    class DrawTouchListener extends MapOnTouchListener {

        public DrawTouchListener(Context context, MapView view) {
            super(context, view);
        }

        @Override
        public boolean onSingleTap(MotionEvent point) {
            Point ptCurrent = mapView.toMapPoint(new Point(point.getX(), point.getY()));
            if (ptStart == null)
                drawLayer.removeAll();//第一次开始前，清空全部graphic
            if (toolType == MEASURE_AREA || toolType == MEASURE_LENGTH) {//绘制线或多边形
                points.add(ptCurrent);//将当前点加入点集合中
                if (ptStart == null) {//画线或多边形的第一个点
                    ptStart = ptCurrent;
                    //绘制第一个点
                    Graphic graphic = new Graphic(ptStart, markerSymbol);
                    drawLayer.addGraphic(graphic);
                } else {//画线或多边形的其他点
                    //绘制其他点
                    Graphic graphic = new Graphic(ptCurrent, markerSymbol);
                    drawLayer.addGraphic(graphic);
                    //生成当前线段（由当前点和上一个点构成）
                    Line line = new Line();
                    line.setStart(ptPrevious);
                    line.setEnd(ptCurrent);
                    if (geoType == Geometry.Type.POLYLINE) {
                        //绘制当前线段
                        Polyline polyline = new Polyline();
                        polyline.addSegment(line, true);
                        Graphic g = new Graphic(polyline, lineSymbol);
                        drawLayer.addGraphic(g);
                        // 计算当前线段的长度
                        double length = GeometryEngine.geodesicLength(polyline, mapView.getSpatialReference(), null);//new LinearUnit(LinearUnit.Code.METER)
                        TextSymbol textSymbol = new TextSymbol(fontSize, getLengthString(length), fontColor);
                        textSymbol.setFontFamily(fontFamily);
                        Graphic label = new Graphic(ptCurrent, textSymbol);
                        drawLayer.addGraphic(label);
                        //Toast.makeText(map.getContext(), formatLength, Toast.LENGTH_SHORT).show();
                    } else {
                        //绘制临时多边形
                        if (tempPolygon == null)
                            tempPolygon = new Polygon();
                        tempPolygon.addSegment(line, false);
                        drawLayer.removeAll();
                        Graphic g = new Graphic(tempPolygon, fillSymbol);
                        drawLayer.addGraphic(g);
                        //计算当前面积
                        double area = GeometryEngine.geodesicArea(tempPolygon, mapView.getSpatialReference(), new AreaUnit(AreaUnit.Code.SQUARE_METER));
                        String sArea = getAreaString(area);
                        TextSymbol textSymbol = new TextSymbol(fontSize, sArea, fontColor);
                        textSymbol.setFontFamily(fontFamily);
                        Graphic label = new Graphic(ptCurrent, textSymbol);
                        drawLayer.addGraphic(label);
                    }
                }
                ptPrevious = ptCurrent;
                return true;
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent point) {
            drawLayer.removeAll();
            if (toolType == MEASURE_LENGTH) {
                Polyline polyline = new Polyline();
                Point startPoint = null;
                Point endPoint = null;
                // 绘制完整的线段
                if (points.size() > 1) {
                    for (int i = 1; i < points.size(); i++) {
                        startPoint = points.get(i - 1);
                        endPoint = points.get(i);
                        Line line = new Line();
                        line.setStart(startPoint);
                        line.setEnd(endPoint);
                        polyline.addSegment(line, false);
                    }
                    Graphic g = new Graphic(polyline, lineSymbol);
                    drawLayer.addGraphic(g);
                    // 计算总长度
                    double length = GeometryEngine.geodesicLength(polyline, mapView.getSpatialReference(), null);//new LinearUnit(LinearUnit.Code.METER)
                    TextSymbol textSymbol = new TextSymbol(fontSize, getLengthString(length), fontColor);
                    textSymbol.setFontFamily(fontFamily);
                    Graphic label = new Graphic(endPoint, textSymbol);
                    drawLayer.addGraphic(label);
                }
                //Toast.makeText(map.getContext(), formatLength, Toast.LENGTH_SHORT).show();
            } else if (toolType == MEASURE_AREA) {
                if (points.size() > 2) {
                    Polygon polygon = new Polygon();
                    Point startPoint;
                    Point endPoint;
                    Point centerPoint = points.get(0);
                    // 绘制完整的多边形
                    for (int i = 1; i < points.size(); i++) {
                        startPoint = points.get(i - 1);
                        endPoint = points.get(i);
                        centerPoint.setX((centerPoint.getX() + endPoint.getX()) * 0.5);
                        centerPoint.setY((centerPoint.getY() + endPoint.getY()) * 0.5);
                        Line line = new Line();
                        line.setStart(startPoint);
                        line.setEnd(endPoint);
                        polygon.addSegment(line, false);
                    }
                    Graphic g = new Graphic(polygon, fillSymbol);
                    drawLayer.addGraphic(g);

                    // 计算总面积
                    double area = GeometryEngine.geodesicArea(tempPolygon, mapView.getSpatialReference(), new AreaUnit(AreaUnit.Code.SQUARE_METER));
                    String sArea = getAreaString(area);
                    TextSymbol textSymbol = new TextSymbol(fontSize, sArea, fontColor);
                    textSymbol.setFontFamily(fontFamily);
                    Graphic label = new Graphic(centerPoint, textSymbol);
                    drawLayer.addGraphic(label);
                }
            }
            // 其他清理工作
            ptStart = null;
            ptPrevious = null;
            points.clear();
            tempPolygon = null;
            return false;
        }

    }

    private String getLengthString(double dValue) {
        String sLength;
        if (dValue < 1000) {
            sLength = String.format("%.1f 米", dValue);
        } else if (dValue >= 1000 && dValue < 10000) {
            sLength = String.format("%.2f 千米", dValue / 1000);
        } else if (dValue >= 10000 && dValue < 100000) {
            sLength = String.format("%.1f 千米", dValue / 1000);
        } else {
            sLength = String.format("%.0f 千米", dValue / 1000);
        }
        //MeasuringTool measuringTool = new MeasuringTool(mMapView);
        return sLength;

    }

    private String getAreaString(double dValue) {
        long area = Math.abs(Math.round(dValue));
        String sArea;
        // 顺时针绘制多边形，面积为正，逆时针绘制，则面积为负
        if (area >= 1000000) {
            double dArea = area / 1000000.0;
            sArea = Double.toString(dArea) + " 平方公里";
        } else
            sArea = Double.toString(area) + " 平方米";
        return sArea;
    }
}