package com.example.administrator.mapdev;

import android.util.Log;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Caizhihuang on 2016/11/13.
 * 照片图层的显示
 * PhotoSurveyManager负责数据的管理，数据展现转移到PhotoSurveyLayer
 */
public class PhotoSurveyLayer extends GraphicsLayer{

    static public String TAG="PhotoSurveyLayer";
    private PhotoSurveyManager photoSurveyManager;

    public PhotoSurveyLayer() {
        super();
    }

    public PhotoSurveyLayer(RenderingMode mode) {
        super(mode);
    }

    public PhotoSurveyLayer(MarkerRotationMode rotationMode) {
        super(rotationMode);
    }

    public PhotoSurveyLayer(SpatialReference sr, Envelope fullextent) {
        super(sr, fullextent);
    }

    public PhotoSurveyLayer(SpatialReference sr, Envelope fullextent, RenderingMode mode) {
        super(sr, fullextent, mode);
    }


    /**
     * 获得采集字段（属性编辑等需要使用）
     * @return
     */
    public List<Field> getFields() {
        return photoSurveyManager.getFields();
    }
}
