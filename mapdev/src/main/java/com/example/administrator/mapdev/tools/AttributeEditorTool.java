package com.example.administrator.mapdev.tools;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.GroupLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.map.Feature;
import com.esri.core.map.Field;
import com.esri.core.map.Graphic;
import com.esri.core.table.FeatureTable;
import com.esri.core.table.TableException;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.query.QueryParameters;
import com.example.administrator.mapdev.LayersManager;
import com.example.administrator.mapdev.R;
import com.example.administrator.mapdev.SurveyDataLayer;
import com.example.administrator.mapdev.UI.AttributeDialog;

import java.util.List;
import java.util.Map;

/**
 * 属性编辑工具，支持编辑GraphicLayer及FeatureLayer
 * 对于FeatureLayer图层，如果支持图层
 * Created by Administrator on 2016/12/4.
 */
public class AttributeEditorTool extends BaseTool {

    private int selectColor = Color.YELLOW;
    private int selectColorWidth = 2;
    private int tolerance = 10;
    private String TAG="AttributeEditorTool";

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

    class FeatureSelection {
        Layer layer;
        List<Field> fields;
        Feature feature;

        public Layer getLayer() {
            return layer;
        }

        public void setLayer(Layer layer) {
            this.layer = layer;
        }

        public List<Field> getFields() {
            return fields;
        }

        public void setFields(List<Field> fields) {
            this.fields = fields;
        }

        public Feature getFeature() {
            return feature;
        }

        public void setFeature(Feature feature) {
            this.feature = feature;
        }
    }

    class TouchListener extends MapOnTouchListener {

        private FeatureSelection featureSelection = null;

        public TouchListener(Context context, MapView view) {
            super(context, view);
        }

        @Override
        public boolean onSingleTap(MotionEvent point) {
            //单击地图，判断是否选中要素对象
            featureSelection = null;
            if (queryAttribute(point)) {
                showAttributeDialog();
                return true;
            }
            return super.onSingleTap(point);
        }

        @Override
        public boolean onDoubleTap(MotionEvent point) {
            //双击地图，判断是否选中要素对象
            featureSelection = null;
            if (queryAttribute(point)) {
                showAttributeDialog();
                return true;
            }
            return super.onDoubleTap(point);
        }

        private boolean queryAttribute(MotionEvent point) {
            clearSelection();
            //先处理采集数据图层
            if (queryGraphicLayerAttribute(point, tolerance))
                return true;
            //然后处理矢量图层
            return queryFeatureLayerAttribute(point, tolerance);
        }

        private boolean queryGraphicLayerAttribute(MotionEvent point, int tolerance) {
            GroupLayer groupLayer = layersManager.getDynamicGroupLayer();
            Layer[] layers = groupLayer.getLayers();
            for (int i = 0; i < layers.length; i++) {
                GraphicsLayer graphicsLayer = (GraphicsLayer) layers[i];
                if (!graphicsLayer.isVisible())
                    continue;
                int[] featureIDs = graphicsLayer.getGraphicIDs(point.getX(), point.getY(), tolerance, 1);
                if (featureIDs.length < 1)
                    continue;
                graphicsLayer.setSelectedGraphics(featureIDs, true);
                graphicsLayer.setSelectionColor(selectColor);
                graphicsLayer.setSelectionColorWidth(selectColorWidth);
                Feature feature = graphicsLayer.getGraphic(featureIDs[0]);
                if (graphicsLayer instanceof SurveyDataLayer) {
                    featureSelection = new FeatureSelection();
                    featureSelection.setFeature(feature);
                    featureSelection.setLayer(graphicsLayer);
                    SurveyDataLayer surveyDataLayer = (SurveyDataLayer) graphicsLayer;
                    featureSelection.setFields(surveyDataLayer.getFields());
                }
                return true;
            }
            return false;
        }

        private boolean queryFeatureLayerAttribute(MotionEvent point, int tolerance) {
            GroupLayer groupLayer = layersManager.getVectorGroupLayer();
            Layer[] layers = groupLayer.getLayers();
            for (int i = 0; i < layers.length; i++) {
                FeatureLayer featureLayer = (FeatureLayer) layers[i];
                if (!featureLayer.isVisible())
                    continue;
                long[] featureIDs = featureLayer.getFeatureIDs(point.getX(), point.getY(), tolerance, 1);
                if (featureIDs.length < 1)
                    continue;
                featureLayer.selectFeatures(featureIDs, true);
                featureLayer.setSelectionColor(selectColor);
                featureLayer.setSelectionColorWidth(selectColorWidth);
                Feature feature = featureLayer.getFeature(featureIDs[0]);
                featureSelection = new FeatureSelection();
                featureSelection.setFeature(feature);
                featureSelection.setLayer(featureLayer);
                featureSelection.setFields(featureLayer.getFeatureTable().getFields());
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

        private void saveAttribute(Map<String,Object> attributes) {
            if( featureSelection == null )
                return ;
            Layer layer = featureSelection.getLayer();
            Feature feature = featureSelection.getFeature();
            if( layer instanceof GraphicsLayer){
                GraphicsLayer graphicsLayer=(GraphicsLayer)layer;
                Graphic graphic = (Graphic)feature;
                //更新并持久化属性
                graphicsLayer.updateGraphic(graphic.getUid(),attributes);
            }else if( layer instanceof FeatureLayer) {
                FeatureLayer featureLayer = (FeatureLayer)layer;
                if( !featureLayer.getFeatureTable().isEditable() ){
                    //只读不能更新的模式
                    //TODO 对于Shp格式的FeatureTable由于目前是只读的，因此将其数据导入到GraphicLayer图层
                    featureLayer.setFeatureVisible(feature.getId(),false);
                    saveFeatureToSurveyLayer(feature,attributes);
                }else{
                    FeatureTable featureTable = featureLayer.getFeatureTable();
                    try {
                        featureTable.updateFeature(feature.getId(), feature);
                    }catch (TableException e){
                        Log.d(TAG, e.getMessage());
                    }
                }
            }
        }

        /**
         * 保存只读feature到采集数据层
         * @param feature
         */
        private void saveFeatureToSurveyLayer(Feature feature,Map<String,Object> attributes){
            Geometry.Type type = feature.getGeometry().getType();
            if( type == Geometry.Type.POINT || type == Geometry.Type.MULTIPOINT ){
                SurveyDataLayer surveyDataLayer = layersManager.getSurveyPointLayer();
                Graphic graphic = new Graphic(feature.getGeometry(),surveyDataLayer.getMarkerSymbol(),attributes);
                surveyDataLayer.addGraphic(graphic);
            }else if( type== Geometry.Type.POLYLINE || type== Geometry.Type.LINE) {
                SurveyDataLayer surveyDataLayer = layersManager.getSurveyPolylineLayer();
                Graphic graphic = new Graphic(feature.getGeometry(),surveyDataLayer.getLineSymbol(),attributes);
                surveyDataLayer.addGraphic(graphic);
            }else if( type== Geometry.Type.POLYGON || type == Geometry.Type.ENVELOPE ){
                SurveyDataLayer surveyDataLayer = layersManager.getSurveyPolygonLayer();
                Graphic graphic = new Graphic(feature.getGeometry(),surveyDataLayer.getFillSymbol(),attributes);
                surveyDataLayer.addGraphic(graphic);
            }
        }

        private void showAttributeDialog() {
            //显示属性编辑对话框
            Context context = mapView.getContext();
            AttributeDialog attributeDialog = new AttributeDialog(context);
            attributeDialog.setTitle(R.string.attribute_editor);
            attributeDialog.setYesOnclickListener(new AttributeDialog.onYesOnclickListener() {
                @Override
                public boolean onYesClick(Map<String,Object> attributes) {
                    clearSelection();
                    saveAttribute(attributes);
                    return true;
                }
            });
            attributeDialog.setNoOnclickListener(new AttributeDialog.onNoOnclickListener() {
                @Override
                public void onNoClick() {
                    clearSelection();
                }
            });
            attributeDialog.setAttribute(featureSelection.getFields(), featureSelection.getFeature());
            attributeDialog.show();
        }
    }
}
