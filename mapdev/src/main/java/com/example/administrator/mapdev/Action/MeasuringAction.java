package com.example.administrator.mapdev.Action;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.esri.android.map.CalloutPopupWindow;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.AreaUnit;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.Unit;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.FillSymbol;
import com.esri.core.symbol.LineSymbol;
import com.esri.core.symbol.MarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.example.administrator.mapdev.R;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 测量工具类
 * Created by caizhihuan on 2016/11/17.
 */
public class MeasuringAction implements ActionMode.Callback, OnSingleTapListener {
    private static final long serialVersionUID = 1L;
    private static final int MENU_DELETE = 0;
    private static final int MENU_PREF = 1;
    private static final int MENU_UNDO = 2;
    private MapView mMap;
    private GraphicsLayer mLayer;
    private OnSingleTapListener mOldOnSingleTapListener;
    private MarkerSymbol mMarkerSymbol;
    private LineSymbol mLineSymbol;
    private double mResult;
    private TextView mText;
    private MeasureType mMeasureMode;
    private int mCurrentLinearUnit;
    private Unit[] mLinearUnits;
    private Unit[] mDefaultLinearUnits;
    private int mCurrentAreaUnit;
    private Unit[] mAreaUnits;
    private Unit[] mDefaultAreaUnits;
    private Context mContext;
    private ArrayList<Point> mPoints;
    private FillSymbol mFillSymbol;
    private CalloutPopupWindow mCallout;
    private ActionMode mMode;
    private Polyline mLine;
    private Polygon mPolygon;

    public MeasuringAction(MapView map) {
        this.mMeasureMode = MeasureType.LINEAR;
        this.mMap = map;
        this.mContext = this.mMap.getContext();
        this.mMarkerSymbol = new SimpleMarkerSymbol(-65536, 10, SimpleMarkerSymbol.STYLE.CIRCLE);
        this.mLineSymbol = new SimpleLineSymbol(-16777216, 3.0F);
        this.mDefaultLinearUnits = new Unit[]{Unit.create(9001), Unit.create(9036), Unit.create(9002), Unit.create(9093)};
        this.mDefaultAreaUnits = new Unit[]{Unit.create(109404), Unit.create(109414), Unit.create(109405), Unit.create(109439)};
        this.mFillSymbol = new SimpleFillSymbol(Color.argb(100, 225, 225, 0));
        this.mFillSymbol.setOutline(new SimpleLineSymbol(0, 0.0F));
    }

    private void init() {
        this.mOldOnSingleTapListener = this.mMap.getOnSingleTapListener();
        this.mMap.setOnSingleTapListener(this);
        this.mLayer = new GraphicsLayer();
        this.mMap.addLayer(this.mLayer);
        this.mPoints = new ArrayList();
    }

    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                this.deleteAll();
                break;
            case 2:
                this.undo();
        }

        return false;
    }

    private void deleteAll() {
        this.mLayer.removeAll();
        this.mResult = 0.0D;
        this.mPoints.clear();
        this.showResult();
        this.updateMenu();
    }

    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        this.mMode = mode;
        this.init();
        MenuItem item = menu.add(0, 2, 1, R.string.undo);
        item.setIcon(R.drawable.ic_undo_24dp);
        item.setVisible(false);
        item = menu.add(0, 0, 2, R.string.clear);
        item.setIcon(R.drawable.ic_clear_24dp);
        item.setVisible(false);
        item = menu.add(0, 1, 3, R.string.measure_type);
        item.setIcon(R.drawable.ic_action_distance);
        MenuItemCompat.setActionProvider(item, new MeasuringAction.Preferences(this.mContext));
        //item.setActionProvider();
        return true;
    }

    public void onDestroyActionMode(ActionMode mode) {
        this.hideCallout();
        this.mMap.removeLayer(this.mLayer);
        this.mLayer = null;
        this.mMap.setOnSingleTapListener(this.mOldOnSingleTapListener);
        this.mPoints = null;
    }

    private void hideCallout() {
        if (this.mCallout != null && this.mCallout.isShowing()) {
            this.mCallout.hide();
        }

    }

    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    public void onSingleTap(float x, float y) {
        this.addPoint(x, y);
    }

    private void addPoint(float x, float y) {
        Point point = this.mMap.toMapPoint(x, y);
        this.mPoints.add(point);
        this.clearAndDraw();
    }

    private void undo() {
        this.mPoints.remove(this.mPoints.size() - 1);
        this.clearAndDraw();
    }

    private void clearAndDraw() {
        int[] oldGraphics = this.mLayer.getGraphicIDs();
        this.draw();
        this.mLayer.removeGraphics(oldGraphics);
        this.updateMenu();
    }

    private void draw() {
        if (this.mPoints.size() != 0) {
            int index = 0;
            this.mResult = 0.0D;
            this.mLine = new Polyline();
            this.mPolygon = new Polygon();

            Point screenPoint;
            for (Iterator labelPointForPolygon = this.mPoints.iterator(); labelPointForPolygon.hasNext(); ++index) {
                screenPoint = (Point) labelPointForPolygon.next();
                this.mLayer.addGraphic(new Graphic(screenPoint, this.mMarkerSymbol, 100));
                if (index == 0) {
                    this.mLine.startPath(screenPoint);
                    if (this.mMeasureMode == MeasuringAction.MeasureType.AREA) {
                        this.mPolygon.startPath(screenPoint);
                    }
                } else {
                    this.mLine.lineTo(screenPoint);
                    if (this.mMeasureMode == MeasuringAction.MeasureType.AREA) {
                        this.mPolygon.lineTo(screenPoint);
                    }
                }

                this.mLayer.addGraphic(new Graphic(this.mLine, this.mLineSymbol));
            }

            Point var4;
            if (this.mMeasureMode == MeasuringAction.MeasureType.LINEAR) {
                this.mResult += GeometryEngine.geodesicLength(this.mLine, this.mMap.getSpatialReference(), (LinearUnit) this.getLinearUnit(this.mCurrentLinearUnit));
                var4 = this.mMap.toScreenPoint((Point) this.mPoints.get(index - 1));
                this.showResult((float) var4.getX(), (float) var4.getY());
            } else if (this.mMeasureMode == MeasuringAction.MeasureType.AREA) {
                this.mLine.lineTo((Point) this.mPoints.get(0));
                this.mLayer.addGraphic(new Graphic(this.mLine, this.mLineSymbol));
                this.mPolygon.lineTo((Point) this.mPoints.get(0));
                this.mLayer.addGraphic(new Graphic(this.mPolygon, this.mFillSymbol));
                this.mResult = GeometryEngine.geodesicArea(this.mPolygon, this.mMap.getSpatialReference(), (AreaUnit) this.getAreaUnit(this.mCurrentAreaUnit));
                var4 = GeometryEngine.getLabelPointForPolygon(this.mPolygon, this.mMap.getSpatialReference());
                screenPoint = this.mMap.toScreenPoint(var4);
                this.showResult((float) screenPoint.getX(), (float) screenPoint.getY());
            }

        }
    }

    private void updateMenu() {
        this.mMode.getMenu().findItem(0).setVisible(this.mPoints.size() > 0);
        this.mMode.getMenu().findItem(2).setVisible(this.mPoints.size() > 0);
    }

    private void showResult(float x, float y) {
        if (this.mResult > 0.0D) {
            if (this.mCallout == null) {
                this.mText = new TextView(this.mContext);
                this.mCallout = new CalloutPopupWindow(this.mText);
            }

            this.mText.setText(this.getResultString());
            this.mCallout.showCallout(this.mMap, this.mMap.toMapPoint(x, y), 0, 0);
        } else if (this.mCallout != null && this.mCallout.isShowing()) {
            this.mCallout.hide();
        }

    }

    private void showResult() {
        if (this.mResult > 0.0D) {
            this.mText.setText(this.getResultString());
            this.mCallout.showCallout(this.mMap);
        } else if (this.mCallout.isShowing()) {
            this.mCallout.hide();
        }

    }

    public void setLinearUnits(Unit[] linearUnits) {
        this.mLinearUnits = linearUnits;
    }

    Unit getLinearUnit(int position) {
        return this.mLinearUnits == null ? this.mDefaultLinearUnits[position] : this.mLinearUnits[position];
    }

    int getAreaUnitSize() {
        return this.mAreaUnits == null ? this.mDefaultAreaUnits.length : this.mAreaUnits.length;
    }

    public void setAreaUnits(Unit[] areaUnits) {
        this.mAreaUnits = areaUnits;
    }

    Unit getAreaUnit(int position) {
        return this.mAreaUnits == null ? this.mDefaultAreaUnits[position] : this.mAreaUnits[position];
    }

    int getLinearUnitSize() {
        return this.mLinearUnits == null ? this.mDefaultLinearUnits.length : this.mLinearUnits.length;
    }

    int getUnitSize() {
        return this.mMeasureMode == MeasuringAction.MeasureType.LINEAR ? this.getLinearUnitSize() : this.getAreaUnitSize();
    }

    Unit getUnit(int position) {
        return this.mMeasureMode == MeasuringAction.MeasureType.LINEAR ? this.getLinearUnit(position) : this.getAreaUnit(position);
    }

    Unit getCurrentUnit() {
        return this.getUnit(this.mMeasureMode == MeasuringAction.MeasureType.LINEAR ? this.mCurrentLinearUnit : this.mCurrentAreaUnit);
    }

    public void setLineSymbol(LineSymbol symbol) {
        this.mLineSymbol = symbol;
    }

    public void setMarkerSymbol(MarkerSymbol symbol) {
        this.mMarkerSymbol = symbol;
    }

    public void setFillSymbol(FillSymbol symbol) {
        this.mFillSymbol = symbol;
    }

    MultiPath getGeometry() {
        return (MultiPath) (this.mMeasureMode == MeasuringAction.MeasureType.LINEAR ? this.mLine : this.mPolygon);
    }

    private String getResultString() {
        return this.mResult > 0.0D ? String.format("%.2f", new Object[]{Double.valueOf(this.mResult)}) + " " + this.getCurrentUnit().getAbbreviation() : "";
    }

    class Preferences extends ShareActionProvider {
        private ImageView imageView;

        public Preferences(Context context) {
            super(context);
            this.imageView = new ImageView(MeasuringAction.this.mContext);
            this.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_start));
        }

        public View onCreateActionView() {
            Spinner spinner = new Spinner(MeasuringAction.this.mContext);
            spinner.setAdapter(new BaseAdapter() {
                public View getView(int position, View convertView, ViewGroup parent) {
                    return Preferences.this.imageView;
                }

                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    LinearLayout layout;
                    TextView text;
                    if (position == 0) {
                        layout = new LinearLayout(MeasuringAction.this.mContext);
                        layout.setOrientation(LinearLayout.HORIZONTAL);
                        text = new TextView(MeasuringAction.this.mContext);
                        text.setText(R.string.measure_type);
                        text.setTextColor(MeasuringAction.this.mContext.getResources().getColor(R.color.accent));
                        text.setTextSize(16.0F);
                        layout.addView(text);
                        RadioButton var9 = new RadioButton(MeasuringAction.this.mContext);
                        var9.setText(R.string.measure_distance);
                        RadioButton var10 = new RadioButton(MeasuringAction.this.mContext);
                        var10.setText(R.string.measure_area);
                        RadioGroup var11 = new RadioGroup(MeasuringAction.this.mContext);
                        var11.addView(var9);
                        var11.addView(var10);
                        var11.check(MeasuringAction.this.mMeasureMode == MeasuringAction.MeasureType.LINEAR ? var9.getId() : var10.getId());
                        layout.addView(var11);
                        layout.setPadding(10, 10, 10, 10);
                        var11.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            public void onCheckedChanged(RadioGroup rGroup, int checkedId) {
                                for (int i = 0; i < rGroup.getChildCount(); ++i) {
                                    if (rGroup.getChildAt(i).getId() == checkedId) {
                                        MeasuringAction.this.mMeasureMode = MeasuringAction.MeasureType.getType(i);
                                        notifyDataSetChanged();
                                        MeasuringAction.this.clearAndDraw();
                                    }
                                }

                            }
                        });
                        return layout;
                    } else {
                        layout = new LinearLayout(MeasuringAction.this.mContext);
                        layout.setOrientation(LinearLayout.HORIZONTAL);
                        text = new TextView(MeasuringAction.this.mContext);
                        text.setText(R.string.measure_unit);
                        text.setTextColor(MeasuringAction.this.mContext.getResources().getColor(R.color.accent));
                        text.setTextSize(16.0F);
                        layout.addView(text);
                        RadioGroup group = new RadioGroup(MeasuringAction.this.mContext);

                        for (int i = 0; i < MeasuringAction.this.getUnitSize(); ++i) {
                            RadioButton r = new RadioButton(MeasuringAction.this.mContext);
                            r.setText(MeasuringAction.this.getUnit(i).getDisplayName());
                            group.addView(r);
                            if (i == (MeasuringAction.this.mMeasureMode == MeasuringAction.MeasureType.LINEAR ? MeasuringAction.this.mCurrentLinearUnit : MeasuringAction.this.mCurrentAreaUnit)) {
                                group.check(r.getId());
                            }
                        }

                        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            public void onCheckedChanged(RadioGroup rGroup, int checkedId) {
                                for (int i = 0; i < rGroup.getChildCount(); ++i) {
                                    if (rGroup.getChildAt(i).getId() == checkedId) {
                                        if (MeasuringAction.this.mMeasureMode == MeasuringAction.MeasureType.LINEAR) {
                                            if (MeasuringAction.this.mResult > 0.0D) {
                                                MeasuringAction.this.mResult = Unit.convertUnits(MeasuringAction.this.mResult, MeasuringAction.this.getLinearUnit(MeasuringAction.this.mCurrentLinearUnit), MeasuringAction.this.getLinearUnit(i));
                                                MeasuringAction.this.mCurrentLinearUnit = i;
                                                MeasuringAction.this.showResult();
                                            } else {
                                                MeasuringAction.this.mCurrentLinearUnit = i;
                                            }
                                        } else if (MeasuringAction.this.mResult > 0.0D) {
                                            MeasuringAction.this.mResult = Unit.convertUnits(MeasuringAction.this.mResult, MeasuringAction.this.getAreaUnit(MeasuringAction.this.mCurrentAreaUnit), MeasuringAction.this.getAreaUnit(i));
                                            MeasuringAction.this.mCurrentAreaUnit = i;
                                            MeasuringAction.this.showResult();
                                        } else {
                                            MeasuringAction.this.mCurrentAreaUnit = i;
                                        }
                                    }
                                }

                            }
                        });
                        layout.addView(group);
                        layout.setPadding(10, 10, 10, 10);
                        return layout;
                    }
                }

                public long getItemId(int position) {
                    return (long) position;
                }

                public Object getItem(int position) {
                    return null;
                }

                public int getCount() {
                    return 2;
                }
            });
            return spinner;
        }
    }

    public enum MeasureType {
        LINEAR,
        AREA;

        MeasureType() {
        }

        public static MeasuringAction.MeasureType getType(int i) {
            switch (i) {
                case 0:
                    return LINEAR;
                case 1:
                    return AREA;
                default:
                    return LINEAR;
            }
        }
    }
}
