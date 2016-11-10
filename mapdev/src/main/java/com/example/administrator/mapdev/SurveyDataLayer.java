package com.example.administrator.mapdev;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.Symbol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/11/10.
 */
public class SurveyDataLayer extends GraphicsLayer {

    public enum GeoType {
        UNKNOWN(0),
        POINT(1),
        POLYLINE(2),
        POLYGON(3);
        private int a;
        public int value() {
            return this.a;
        }
        GeoType(int val) {
            this.a = val;
        }
    }

    private SurveyDataManager surveyDataManager;
    private Map<Integer, Long> graphics = new LinkedHashMap<>();
    private GeoType geoType;

    public SurveyDataLayer() {
        super();
    }

    public SurveyDataLayer(RenderingMode mode) {
        super(mode);
    }

    public SurveyDataLayer(MarkerRotationMode rotationMode) {
        super(rotationMode);
    }

    public SurveyDataLayer(SpatialReference sr, Envelope fullextent) {
        super(sr, fullextent);
    }

    public SurveyDataLayer(SpatialReference sr, Envelope fullextent, RenderingMode mode) {
        super(sr, fullextent, mode);
    }

    @Override
    public int addGraphic(Graphic graphic) {
        int id = super.addGraphic(graphic);
        SurveyData data = SurveyDataManager.toSurveyData(graphic);
        surveyDataManager.saveSurveyData(data);
        return id;
    }

    @Override
    public int[] addGraphics(Graphic[] graphics) {
        int[] ids = super.addGraphics(graphics);
        for(int id : ids ) {
            SurveyData data = SurveyDataManager.toSurveyData(graphics[id]);
            surveyDataManager.saveSurveyData(data);
        }
        return ids;
    }

    @Override
    public void updateGraphics(int[] ids, int[] drawOrder) {
        super.updateGraphics(ids, drawOrder);
    }

    @Override
    public void updateGraphic(int id, Graphic graphic) {
        super.updateGraphic(id, graphic);
        SurveyData surveyData = getSurveyData(id);
        long dbIdx = surveyData.getBaseObjId();
        SurveyData newData = SurveyDataManager.toSurveyData(graphic);
        newData.update(dbIdx);
    }

    @Override
    public void updateGraphics(int[] ids, Graphic[] graphics) {
        super.updateGraphics(ids, graphics);
        for(int id : ids ) {
            SurveyData surveyData = getSurveyData(id);
            long dbIdx = surveyData.getBaseObjId();
            SurveyData newData = SurveyDataManager.toSurveyData(graphics[id]);
            newData.update(dbIdx);
        }
    }

    @Override
    public void updateGraphic(int id, Geometry geometry) {
        super.updateGraphic(id, geometry);
        SurveyData surveyData = getSurveyData(id);
        surveyDataManager.updateSurveyData(surveyData,geometry);
    }

    @Override
    public void updateGraphic(int id, Symbol symbol) {
        super.updateGraphic(id, symbol);
        SurveyData surveyData = getSurveyData(id);
        surveyDataManager.updateSurveyData(surveyData, symbol);
    }

    @Override
    public void updateGraphic(int id, Map<String, Object> attributes) {
        super.updateGraphic(id, attributes);
        SurveyData surveyData = getSurveyData(id);
        surveyDataManager.updateSurveyData(surveyData, attributes);
    }

    @Override
    public void updateGraphic(int id, int drawOrder) {
        super.updateGraphic(id, drawOrder);
        SurveyData surveyData = getSurveyData(id);
        surveyData.setDrawOrder(drawOrder);
        surveyData.update(surveyData.getBaseObjId());
    }

    @Override
    public void updateGraphics(int[] ids, int drawOrder) {
        super.updateGraphics(ids, drawOrder);
        for(int id : ids ) {
            SurveyData surveyData = getSurveyData(id);
            surveyData.setDrawOrder(drawOrder);
            surveyData.update(surveyData.getBaseObjId());
        }
    }

    @Override
    public void removeGraphic(int id) {
        super.removeGraphic(id);
        SurveyData surveyData = getSurveyData(id);
        surveyDataManager.deleteSurveyData(surveyData);
    }

    @Override
    public void removeGraphics(int[] ids) {
        super.removeGraphics(ids);
        for(int id : ids ) {
            SurveyData surveyData = getSurveyData(id);
            surveyDataManager.deleteSurveyData(surveyData);
        }
    }

    public GeoType getGeoType() {
        return geoType;
    }

    public void setGeoType(GeoType geoType) {
        this.geoType = geoType;
    }

    public SurveyDataManager getSurveyDataManager() {
        return surveyDataManager;
    }

    public void setSurveyDataManager(SurveyDataManager surveyDataManager) {
        this.surveyDataManager = surveyDataManager;
    }

    /**
     * 加载所有调查数据
     */
    public void loadSurveyDataSet() {
        //只返回数据集合中没有的数据
        List<SurveyData> surveyDatas = surveyDataManager.loadSurveyDataSet(geoType.value());
        if (surveyDatas == null)
            return;
        for (SurveyData data : surveyDatas) {
            Graphic graphic = SurveyDataManager.fromSurveyData(data);
            int id = super.addGraphic(graphic);
            data.setGraphic(graphic);
            graphics.put(id, data.getBaseObjId());
        }
    }

    /**
     * 获得GraphicId对应的
     *
     * @param graphicId
     * @return
     */
    public SurveyData getSurveyData(int graphicId) {
        Long id = graphics.get(graphicId);
        if (id != null) {
            return surveyDataManager.getSurveyData(id);
        }
        return null;
    }

    @Override
    public void removeAll() {
        super.removeAll();
        graphics.clear();
    }


}
