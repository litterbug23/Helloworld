package com.example.administrator.mapdev;

import android.content.Context;
import android.graphics.Color;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/4/22.
 */
public class DrawFeatureTool extends MapOnTouchListener implements ActionMode.Callback {
	private MapView map;
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
	private int fontSize=16;
	private int fontColor=Color.YELLOW;
	private String fontFamily="DroidSansFallback.ttf";

	public DrawFeatureTool(Context context, MapView view) {
		super(context, view);
		map = view;
		drawLayer = (GraphicsLayer) map.getLayer(map.getLayers().length - 1);
		points = new ArrayList<Point>();
		lineSymbol = new SimpleLineSymbol(Color.RED, 3, SimpleLineSymbol.STYLE.DOT);
		markerSymbol = new SimpleMarkerSymbol(Color.BLUE,8, SimpleMarkerSymbol.STYLE.CIRCLE);
		fillSymbol = new SimpleFillSymbol(Color.argb(100, 255, 0, 0), SimpleFillSymbol.STYLE.SOLID);
		SimpleLineSymbol outLine = new SimpleLineSymbol(Color.RED, 1, SimpleLineSymbol.STYLE.SOLID);
		fillSymbol.setOutline(outLine);
	}

	// 根据用户选择设置当前绘制的几何图形类型
	public void setType(String geometryType) {
		if (geometryType.equalsIgnoreCase("Point"))
			this.geoType = Geometry.Type.POINT;
		else if (geometryType.equalsIgnoreCase("Polyline"))
			this.geoType = Geometry.Type.POLYLINE;
		else if (geometryType.equalsIgnoreCase("Polygon"))
			this.geoType = Geometry.Type.POLYGON;
	}

	@Override
	public boolean onDoubleTap(MotionEvent point) {
		return super.onDoubleTap(point);
	}

	@Override
	public boolean onSingleTap(MotionEvent point) {
		return super.onSingleTap(point);
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {

	}
}
