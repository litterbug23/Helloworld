package com.example.administrator.mapdev;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.SpatialReference;

/**
 * Created by Administrator on 2016/11/13.
 * 照片图层的绘制
 */
public class PhotoSurveyLayer extends GraphicsLayer{

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


}
