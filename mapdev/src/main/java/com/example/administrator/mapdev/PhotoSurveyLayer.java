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
    protected List<Field> fields;

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
     * 初始化字段信息（照片的字段信息）
     */
    protected void initFields(){
        //TODO: 暂时固定属性字段
        fields = new ArrayList<>();
        //photoImage;azimuth;altitude;date;staff; comment;
        try {
            Field field = new Field("photoImage", "照片名称",Field.toEsriFieldType( Field.esriFieldTypeString) );
            fields.add(field);
            field = new Field("azimuth", "方位角",Field.toEsriFieldType( Field.esriFieldTypeSingle) );
            fields.add(field);
            field = new Field("altitude", "高程",Field.toEsriFieldType( Field.esriFieldTypeSingle) );
            fields.add(field);
            field = new Field("date", "采集日期",Field.toEsriFieldType( Field.esriFieldTypeDate) );
            fields.add(field);
            field = new Field("staff", "采集人员",Field.toEsriFieldType( Field.esriFieldTypeString) );
            fields.add(field);
            field = new Field("comment", "描述信息",Field.toEsriFieldType( Field.esriFieldTypeString) );
            fields.add(field);
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    /**
     * 获得采集字段（属性编辑等需要使用）
     * @return
     */
    public List<Field> getFields() {
        return fields;
    }
}
