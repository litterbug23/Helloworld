package com.example.administrator.mapdev;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseIntArray;

import com.esri.core.map.Field;

import org.gdal.ogr.ogr;

/**
 * Created by Administrator on 2016/11/10.
 * 主要负责采集数据的导出，目前可以支持的导出类型为shp,kml
 * 其中采集数据导出为shp，
 * 照片及gps数据可以导出为kml（以方便在GoogleEarth等平台查看）
 */
public final class SurveyDataExport {

    Activity container;

    public final static SparseIntArray esriFieldToOgrFieldType = new SparseIntArray() {{
        put(Field.esriFieldTypeInteger, ogr.OFTInteger);
        put(Field.esriFieldTypeSmallInteger, ogr.OFTInteger);
        put(Field.esriFieldTypeDouble, ogr.OFTReal);
        put(Field.esriFieldTypeSingle, ogr.OFTReal);
        put(Field.esriFieldTypeDate, ogr.OFTDateTime);
        put(Field.esriFieldTypeString, ogr.OFTString);
        put(Field.esriFieldTypeBlob, ogr.OFTBinary);
    }};

    public SurveyDataExport(Activity container) {
        this.container =container;
    }

    private SurveyDataManager getSurveyDataManager() {
        try {
            return MapApplication.instance().getLayersManager().getSurveyDataManager();
        } catch (NullPointerException e) {
            Log.d("SurveyDataExport", e.getMessage());
        }
        return null;
    }

    private void showAlertMessage(String title,String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(container, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("确定", null);
        builder.show();
    }

    /**
     * 导出采集数据到制定目录
     */
    public void exportSurveyData() {
        final String title= "采集数据导出";
        final SurveyDataManager surveyDataManager = getSurveyDataManager();
        if (surveyDataManager == null) {
            showAlertMessage(title, "地图未打开，没有采集数据可以导出");
            return;
        }
        final String[] items = new String[]{
                "采集点数据",
                "采集线数据",
                "采集面数据"};
        final boolean[] checkItems = new boolean[]{true, true, true};
        AlertDialog.Builder builder = new AlertDialog.Builder( container , R.style.AppCompatAlertDialogStyle);
        builder.setTitle(title);
        builder.setMultiChoiceItems(items, checkItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checkItems[which] = isChecked;
            }
        });
        builder.setNegativeButton("取消", null);
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (checkItems[0]) {
                    surveyDataManager.exportSurveyData(1);
                    //MapApplication.showMessage(items[0] + "导出完成");
                }
                if (checkItems[1]) {
                    surveyDataManager.exportSurveyData(2);
                    //MapApplication.showMessage(items[1]+"导出完成");
                }
                if (checkItems[2]) {
                    surveyDataManager.exportSurveyData(3);
                    //MapApplication.showMessage(items[2]+"导出完成");
                }
                showAlertMessage(title,"数据导出完成");
            }
        });
        builder.show();
    }
}
