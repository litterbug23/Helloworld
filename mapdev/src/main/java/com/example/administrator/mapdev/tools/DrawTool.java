package com.example.administrator.mapdev.tools;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.FillSymbol;
import com.esri.core.symbol.LineSymbol;
import com.esri.core.symbol.MarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.example.administrator.mapdev.LayersManager;

/**
 * Created by Administrator on 2016/4/22.
 */
public class DrawTool extends BaseTool {

    private GraphicsLayer tempLayer;
    private MarkerSymbol markerSymbol;
    private LineSymbol lineSymbol;
    private FillSymbol fillSymbol;
    private Point point;
    private Envelope envelope;
    private Polyline polyline;
    private Polygon polygon;
    private Graphic drawGraphic;
    private int mUid = 0;
    public static final int POINT = 1;
    public static final int ENVELOPE = 2;
    public static final int POLYLINE = 3;
    public static final int POLYGON = 4;
    public static final int CIRCLE = 5;
    //	public static final int ELLIPSE = 6;
    public static final int FREEHAND_POLYGON = 7;
    public static final int FREEHAND_POLYLINE = 8;

    public DrawTool(LayersManager layersManager) {
        super(layersManager);
        this.tempLayer = layersManager.getUserDrawerLayer();
        mapOnTouchListener = new DrawTouchListener(this.mapView.getContext(),
                this.mapView);

        this.markerSymbol = new SimpleMarkerSymbol(Color.BLACK, 16,
                SimpleMarkerSymbol.STYLE.CIRCLE);
        this.lineSymbol = new SimpleLineSymbol(Color.BLACK, 2);
        this.fillSymbol = new SimpleFillSymbol(Color.BLACK);
        this.fillSymbol.setAlpha(90);
    }

    @Override
    public void activate(int drawType) {
        super.activate(drawType);
        if (this.mapView == null)
            return;
        this.mapView.setOnTouchListener(mapOnTouchListener);
        drawGraphic = null;
        switch (this.toolType) {
            case DrawTool.POINT:
                this.point = new Point();
                drawGraphic = new Graphic(this.point, this.markerSymbol);
                break;
            case DrawTool.ENVELOPE:
                this.envelope = new Envelope();
                drawGraphic = new Graphic(this.envelope, this.fillSymbol);
                break;
            case DrawTool.POLYGON:
            case DrawTool.CIRCLE:
            case DrawTool.FREEHAND_POLYGON:
                this.polygon = new Polygon();
                drawGraphic = new Graphic(this.polygon, this.fillSymbol);
                break;
            case DrawTool.POLYLINE:
            case DrawTool.FREEHAND_POLYLINE:
                this.polyline = new Polyline();
                drawGraphic = new Graphic(this.polyline, this.lineSymbol);
                break;
        }
        if (drawGraphic != null) {
            mUid = this.tempLayer.addGraphic(drawGraphic);
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        //this.tempLayer.clear();
        this.active = false;
        this.toolType = -1;
        this.point = null;
        this.envelope = null;
        this.polygon = null;
        this.polyline = null;
        this.drawGraphic = null;
    }

    public MarkerSymbol getMarkerSymbol() {
        return markerSymbol;
    }

    public void setMarkerSymbol(MarkerSymbol markerSymbol) {
        this.markerSymbol = markerSymbol;
    }

    public LineSymbol getLineSymbol() {
        return lineSymbol;
    }

    public void setLineSymbol(LineSymbol lineSymbol) {
        this.lineSymbol = lineSymbol;
    }

    public FillSymbol getFillSymbol() {
        return fillSymbol;
    }

    public void setFillSymbol(FillSymbol fillSymbol) {
        this.fillSymbol = fillSymbol;
    }

    private void sendDrawEndEvent() {
//        DrawEvent e = new DrawEvent(this, DrawEvent.DRAW_END,
//                DrawTool.this.drawGraphic);
//        DrawTool.this.notifyEvent(e);
        int type = this.toolType;
        this.deactivate();
        this.activate(type);
    }

    class DrawTouchListener extends MapOnTouchListener {

        private Point startPoint;

        public DrawTouchListener(Context context, MapView view) {
            super(context, view);
        }

        public boolean onTouch(View view, MotionEvent event) {
            if (active
                    && (toolType == POINT || toolType == ENVELOPE
                    || toolType == CIRCLE
                    || toolType == FREEHAND_POLYLINE || toolType == FREEHAND_POLYGON)
                    && event.getAction() == MotionEvent.ACTION_DOWN) {
                Point point = mapView.toMapPoint(event.getX(), event.getY());
                switch (toolType) {
                    case DrawTool.POINT:
                        DrawTool.this.point.setXY(point.getX(), point.getY());
                        //sendDrawEndEvent();
                        break;
                    case DrawTool.ENVELOPE:
                        startPoint = point;
                        envelope.setCoords(point.getX(), point.getY(),
                                point.getX(), point.getY());
                        break;
                    case DrawTool.CIRCLE:
                        startPoint = point;
                        break;
                    case DrawTool.FREEHAND_POLYGON:
                        polygon.startPath(point);
                        break;
                    case DrawTool.FREEHAND_POLYLINE:
                        polyline.startPath(point);
                        break;
                }
                if (drawGraphic != null)
                    tempLayer.updateGraphic(mUid, drawGraphic.getGeometry());
            }
            return super.onTouch(view, event);
        }

        public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
            if (active
                    && (toolType == ENVELOPE || toolType == FREEHAND_POLYGON
                    || toolType == FREEHAND_POLYLINE || toolType == CIRCLE)) {
                Point point = mapView.toMapPoint(to.getX(), to.getY());
                switch (toolType) {
                    case DrawTool.ENVELOPE:
                        envelope.setXMin(startPoint.getX() > point.getX() ? point
                                .getX() : startPoint.getX());
                        envelope.setYMin(startPoint.getY() > point.getY() ? point
                                .getY() : startPoint.getY());
                        envelope.setXMax(startPoint.getX() < point.getX() ? point
                                .getX() : startPoint.getX());
                        envelope.setYMax(startPoint.getY() < point.getY() ? point
                                .getY() : startPoint.getY());
                        break;
                    case DrawTool.FREEHAND_POLYGON:
                        polygon.lineTo(point);
                        break;
                    case DrawTool.FREEHAND_POLYLINE:
                        polyline.lineTo(point);
                        break;
                    case DrawTool.CIRCLE:
                        double radius = Math.sqrt(Math.pow(startPoint.getX()
                                - point.getX(), 2)
                                + Math.pow(startPoint.getY() - point.getY(), 2));
                        getCircle(startPoint, radius, polygon);
                        break;
                }
                if (drawGraphic != null)
                    tempLayer.updateGraphic(mUid, drawGraphic.getGeometry());
                return true;
            }
            return super.onDragPointerMove(from, to);
        }

        public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
            if (active && (toolType == ENVELOPE || toolType == FREEHAND_POLYGON
                    || toolType == FREEHAND_POLYLINE || toolType == CIRCLE)) {
                Point point = mapView.toMapPoint(to.getX(), to.getY());
                switch (toolType) {
                    case DrawTool.ENVELOPE:
                        envelope.setXMin(startPoint.getX() > point.getX() ? point
                                .getX() : startPoint.getX());
                        envelope.setYMin(startPoint.getY() > point.getY() ? point
                                .getY() : startPoint.getY());
                        envelope.setXMax(startPoint.getX() < point.getX() ? point
                                .getX() : startPoint.getX());
                        envelope.setYMax(startPoint.getY() < point.getY() ? point
                                .getY() : startPoint.getY());
                        break;
                    case DrawTool.FREEHAND_POLYGON:
                        polygon.lineTo(point);
                        break;
                    case DrawTool.FREEHAND_POLYLINE:
                        polyline.lineTo(point);
                        break;
                    case DrawTool.CIRCLE:
                        double radius = Math.sqrt(Math.pow(startPoint.getX()
                                - point.getX(), 2)
                                + Math.pow(startPoint.getY() - point.getY(), 2));
                        getCircle(startPoint, radius, polygon);
                        break;
                }
                sendDrawEndEvent();
                this.startPoint = null;
                return true;
            }
            return super.onDragPointerUp(from, to);
        }

        public boolean onSingleTap(MotionEvent event) {
            Point point = mapView.toMapPoint(event.getX(), event.getY());
            if (active && (toolType == POLYGON || toolType == POLYLINE)) {
                switch (toolType) {
                    case DrawTool.POLYGON:
                        if (startPoint == null) {
                            this.startPoint = point;
                            polygon.startPath(point);
                        } else
                            polygon.lineTo(point);
                        break;
                    case DrawTool.POLYLINE:
                        if (startPoint == null) {
                            this.startPoint = point;
                            polyline.startPath(point);
                        } else
                            polyline.lineTo(point);
                        break;
                }
                if (drawGraphic != null)
                    tempLayer.updateGraphic(mUid, drawGraphic.getGeometry());
                return true;
            }
            return false;
        }

        public boolean onDoubleTap(MotionEvent event) {
            Point point = mapView.toMapPoint(event.getX(), event.getY());
            if (active && (toolType == POINT || toolType == POLYGON || toolType == POLYLINE)) {
                switch (toolType) {
                    case DrawTool.POINT:
                        DrawTool.this.point = point;
                        break;
                    case DrawTool.POLYGON:
                        polygon.lineTo(point);
                        break;
                    case DrawTool.POLYLINE:
                        polyline.lineTo(point);
                        break;
                }
                sendDrawEndEvent();
                this.startPoint = null;
            }
            return true;
        }

        private void getCircle(Point center, double radius, Polygon circle) {
            circle.setEmpty();
            Point[] points = getPoints(center, radius);
            circle.startPath(points[0]);
            for (int i = 1; i < points.length; i++)
                circle.lineTo(points[i]);
        }

        private Point[] getPoints(Point center, double radius) {
            Point[] points = new Point[50];
            double sin;
            double cos;
            double x;
            double y;
            for (double i = 0; i < 50; i++) {
                sin = Math.sin(Math.PI * 2 * i / 50);
                cos = Math.cos(Math.PI * 2 * i / 50);
                x = center.getX() + radius * sin;
                y = center.getY() + radius * cos;
                points[(int) i] = new Point(x, y);
            }
            return points;
        }
    }
}
