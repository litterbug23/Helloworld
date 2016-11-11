package com.example.administrator.mapdev;

import android.util.SparseIntArray;

import com.esri.core.map.Field;

import org.gdal.ogr.ogr;

/**
 * Created by Administrator on 2016/11/10.
 */
public final class SurveyDataExport {

     public final static SparseIntArray esriFieldToOgrFieldType = new SparseIntArray() {{
        put(Field.esriFieldTypeInteger, ogr.OFTInteger);
        put(Field.esriFieldTypeSmallInteger, ogr.OFTInteger);
        put(Field.esriFieldTypeDouble, ogr.OFTReal);
        put(Field.esriFieldTypeSingle, ogr.OFTReal);
        put(Field.esriFieldTypeDate, ogr.OFTDateTime);
        put(Field.esriFieldTypeString, ogr.OFTString);
        put(Field.esriFieldTypeBlob, ogr.OFTBinary);
    }};


}
