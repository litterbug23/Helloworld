package com.example.administrator.mapdev.tools;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Feature;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.example.administrator.mapdev.LayersManager;
import com.esri.core.geometry.Geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * 几何形状编辑
 * Created by caizhihuang on 2016/4/23.
 */
public class GeometryEditorTool extends BaseTool {

    //编辑样式
    private SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(Color.BLUE, 10, SimpleMarkerSymbol.STYLE.DIAMOND);
    private SimpleLineSymbol lineSymbol = new SimpleLineSymbol(Color.YELLOW, 3);
    //编辑绘制相关
    private List<Point> undoHistories = new ArrayList<>();  //撤销
    private List<Point> redoHistories = new ArrayList<>();  //重做
    private Polyline polyline = null;
    private MultiPoint multiPoint = null;
    private Graphic polylineGraphic = null;
    private Graphic pointsGraphic = null;
    private GraphicsLayer graphicsLayer;
    private int pointsGID=0;
    private int polylineGID=0;
    //编辑要素对象
    private Feature feature;

    public GeometryEditorTool(LayersManager layersManager) {
        super(layersManager);
        this.mapOnTouchListener = new TouchListener(mapView.getContext(),mapView);
    }

    @Override
    public boolean isActive() {
        return super.isActive();
    }

    @Override
    public int getToolType() {
        return super.getToolType();
    }

    @Override
    public void activate(int toolType) {
        super.activate(toolType);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    public void redo() {

    }

    public void undo() {

    }

    /**
     * 初始化编辑工具
     */
    private void initEditorTool(){
        Geometry geometry = feature.getGeometry();

    }

    /**
     * 编辑操作：
     * 1、长按顶点，选中顶点，缺省开始顶点编辑
     * 2、长按边线，在边线上增加一个顶点
     * 3、双击顶点，可以删除一个顶点
     */
    class TouchListener extends MapOnTouchListener {

        public TouchListener(Context context, MapView view) {
            super(context, view);
        }

        @Override
        public boolean onSingleTap(MotionEvent point) {
            return super.onSingleTap(point);
        }

        @Override
        public void onLongPress(MotionEvent point) {
            super.onLongPress(point);
        }

        @Override
        public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
            return super.onDragPointerMove(from, to);
        }
    }
}
