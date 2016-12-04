package com.example.administrator.mapdev.tools;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.GroupLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Point;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.query.QueryParameters;
import com.example.administrator.mapdev.LayersManager;
import com.example.administrator.mapdev.R;
import com.example.administrator.mapdev.UI.AttributeDialog;

/**
 * 属性编辑工具，支持编辑GraphicLayer及FeatureLayer
 * 对于FeatureLayer图层，如果支持图层
 * Created by Administrator on 2016/12/4.
 */
public class AttributeEditorTool extends BaseTool {

    private int selectColor = Color.YELLOW;
    private int selectColorWidth=2;
    private int tolerance=10;

    public AttributeEditorTool(LayersManager layersManager) {
        super(layersManager);
        this.mapOnTouchListener = new TouchListener(mapView.getContext(), mapView);
    }

    @Override
    public boolean isActive() {
        return super.isActive();
    }

    @Override
    public int getToolType() {
        return super.getToolType();
    }


    public void activate() {
        if (isActive())
            return;
        super.activate(toolType);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    class TouchListener extends MapOnTouchListener {

        public TouchListener(Context context, MapView view) {
            super(context, view);
        }

        @Override
        public boolean onDoubleTap(MotionEvent point) {
            //双击地图，判断是否选中要素对象
            if( queryAttribute(point) )
            {
                showAttributeDialog();
                return true;
            }
            return super.onDoubleTap(point);
        }

        private boolean queryAttribute(MotionEvent point) {
            clearSelection();
            //先处理采集数据图层
            if( queryGraphicLayerAttribute(point, tolerance))
                return true;
            //然后处理矢量图层
            return queryFeatureLayerAttribute(point, tolerance);
        }

        private boolean queryGraphicLayerAttribute(MotionEvent point,int tolerance) {
            GroupLayer groupLayer = layersManager.getDynamicGroupLayer();
            Layer[] layers = groupLayer.getLayers();
            for (int i = 0; i < layers.length; i++) {
                GraphicsLayer graphicsLayer = (GraphicsLayer) layers[i];
                if( !graphicsLayer.isVisible() )
                    continue;
                int[] featureIDs = graphicsLayer.getGraphicIDs(point.getX(),point.getY(),tolerance);
                if(featureIDs.length<1)
                    continue;
                graphicsLayer.setSelectedGraphics(featureIDs, true);
                graphicsLayer.setSelectionColor(selectColor);
                graphicsLayer.setSelectionColorWidth(selectColorWidth);
                return true;
            }
            return false;
        }

        private boolean queryFeatureLayerAttribute(MotionEvent point,int tolerance) {
            GroupLayer groupLayer = layersManager.getVectorGroupLayer();
            Layer[] layers = groupLayer.getLayers();
            for (int i = 0; i < layers.length; i++) {
                FeatureLayer featureLayer = (FeatureLayer) layers[i];
                if( !featureLayer.isVisible() )
                    continue;
                long[] featureIDs = featureLayer.getFeatureIDs(point.getX(), point.getY(), tolerance);
                if(featureIDs.length<1)
                    continue;
                featureLayer.selectFeatures(featureIDs, true);
                featureLayer.setSelectionColor(selectColor);
                featureLayer.setSelectionColorWidth(selectColorWidth);
                return true;
            }
            return false;
        }

        private void clearSelection() {
            //清楚选中效果
            GroupLayer groupLayer = layersManager.getDynamicGroupLayer();
            Layer[] layers = groupLayer.getLayers();
            for (int i = 0; i < layers.length; i++) {
                GraphicsLayer graphicsLayer = (GraphicsLayer) layers[i];
                graphicsLayer.clearSelection();
            }
            groupLayer = layersManager.getVectorGroupLayer();
            layers = groupLayer.getLayers();
            for (int i = 0; i < layers.length; i++) {
                FeatureLayer featureLayer = (FeatureLayer) layers[i];
                featureLayer.clearSelection();
            }
        }

        private void showAttributeDialog(){
            //显示属性编辑对话框
            Context context = mapView.getContext();
            AttributeDialog attributeDialog = new AttributeDialog(context);
            attributeDialog.setTitle(R.string.attribute_editor);
            attributeDialog.setYesOnclickListener(new AttributeDialog.onYesOnclickListener() {
                @Override
                public boolean onYesClick() {
                    clearSelection();
                    return true;
                }
            });
            attributeDialog.setNoOnclickListener(new AttributeDialog.onNoOnclickListener() {
                @Override
                public void onNoClick() {
                    clearSelection();
                }
            });
            attributeDialog.show();
        }
    }
}
