package com.example.administrator.mapdev;

import android.graphics.Color;
import android.util.Log;
import android.util.SparseIntArray;

import com.esri.android.map.MapView;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.GeometryUtil;
import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.map.Field;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.example.administrator.mapdev.tools.FeatureLayerUtils;
import com.example.administrator.mapdev.tools.GeometryUtils;

import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;
import org.litepal.crud.DataSupport;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConstants;

/**
 * Created by Administrator on 2016/4/22.
 * 数据管理方式与PhotoSurvey类似
 * 1、新建数据后，直接存入数据库中
 * 2、从数据库中加载数据，排除当前已经加载的数据
 */
public final class SurveyDataManager {

    private Map<Long, SurveyData> surveyDataMap = new LinkedHashMap<>();
    private Map<String, Integer> surveyFields = new LinkedHashMap<>();
    public static SparseIntArray esriToOgrFieldType = new SparseIntArray() {{
        put(Field.esriFieldTypeInteger, ogr.OFTInteger);
        put(Field.esriFieldTypeSmallInteger, ogr.OFTInteger);
        put(Field.esriFieldTypeDouble, ogr.OFTReal);
        put(Field.esriFieldTypeSingle, ogr.OFTReal);
        put(Field.esriFieldTypeDate, ogr.OFTDateTime);
        put(Field.esriFieldTypeString, ogr.OFTString);
        put(Field.esriFieldTypeBlob, ogr.OFTBinary);
    }};

    public SurveyDataManager() {
        initAttributeFields();
        testSave();
    }

    /**
     * 初始化字段类型表（暂时使用固定类型的字段)
     */
    private void initAttributeFields() {
        surveyFields.put("XZQDM", Field.esriFieldTypeInteger);
        surveyFields.put("XMC", Field.esriFieldTypeString);
        surveyFields.put("JCBH", Field.esriFieldTypeInteger);
        surveyFields.put("TBLX", Field.esriFieldTypeInteger);
        surveyFields.put("TZ", Field.esriFieldTypeString);
        surveyFields.put("QSX", Field.esriFieldTypeString);
        surveyFields.put("HSX", Field.esriFieldTypeString);
        surveyFields.put("XZB", Field.esriFieldTypeDouble);
        surveyFields.put("YZB", Field.esriFieldTypeDouble);
        surveyFields.put("JCMJ", Field.esriFieldTypeDouble);
        surveyFields.put("BGDL", Field.esriFieldTypeString);
        surveyFields.put("BGFW", Field.esriFieldTypeString);
        surveyFields.put("WBGLX", Field.esriFieldTypeString);
        surveyFields.put("BZ", Field.esriFieldTypeString);
    }

    public SurveyData getSurveyData(long id) {
        return surveyDataMap.get(id);
    }

    /**
     * 将graphic数据转换成SurveyData格式
     *
     * @param graphic
     */
    static public SurveyData toSurveyData(Graphic graphic) {
        if (graphic == null || graphic.getGeometry() == null)
            return null;
        SurveyData surveyData = new SurveyData();
        surveyData.setGraphic(graphic);
        surveyData.setGeoType(SurveyData.getGeoType(graphic.getGeometry()));
        try {
            {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos));
                oos.writeObject(graphic.getGeometry());
                oos.close();
                byte[] bytes = bos.toByteArray();
                bos.close();
                surveyData.setGeometry(bytes);
            }
            if (graphic.getSymbol() != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos));
                bos.reset();
                oos.reset();
                oos.writeObject(graphic.getSymbol());
                oos.close();
                byte[] bytes = bos.toByteArray();
                bos.close();
                surveyData.setSymbolStyle(bytes);
            }
            if (graphic.getAttributes() != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos));
                bos.reset();
                oos.reset();
                oos.writeObject(graphic.getAttributes());
                oos.close();
                byte[] bytes = bos.toByteArray();
                bos.close();
                surveyData.setAttributes(bytes);
            }
            surveyData.setDrawOrder(graphic.getDrawOrder());
            surveyData.setSurveyDate(new Date());
            MapScene mapScene = MapApplication.instance().getLayersManager().getCurrentScene();
            surveyData.setMapScene(mapScene);
        } catch (IOException e) {
            Log.d("SurveyData", e.getMessage());
        }
        return surveyData;
    }

    static public Graphic fromSurveyData(SurveyData data) {
        if (data == null || data.getGeometry() == null)
            return null;
        if (data.getGraphic() != null)
            return data.getGraphic();
        try {
            Symbol symbol = null;
            Map<String, Object> attributes = null;
            Geometry geometry = null;
            {
                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(data.getGeometry());
                java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new BufferedInputStream(bais));      //something wrong here
                geometry = (Geometry) ois.readObject();
                ois.close();
                bais.close();
            }
            if (data.getSymbolStyle() != null) {
                java.io.ByteArrayInputStream symbol_oais = new java.io.ByteArrayInputStream(data.getSymbolStyle());
                java.io.ObjectInputStream symbol_ois = new java.io.ObjectInputStream(new BufferedInputStream(symbol_oais));      //something wrong here
                symbol = (Symbol) symbol_ois.readObject();
                symbol_oais.close();
                symbol_ois.close();
            }
            if (data.getAttributes() != null) {
                java.io.ByteArrayInputStream attr_oais = new java.io.ByteArrayInputStream(data.getAttributes());
                java.io.ObjectInputStream attr_ois = new java.io.ObjectInputStream(new BufferedInputStream(attr_oais));      //something wrong here
                attributes = (Map<String, Object>) attr_ois.readObject();
                attr_ois.close();
                attr_oais.close();
            }
            Graphic graphic = new Graphic(geometry, symbol, attributes, data.getDrawOrder());
            return graphic;
        } catch (StreamCorruptedException e) {
            Log.d("SurveyData", e.getMessage());
        } catch (IOException e) {
            Log.d("SurveyData", e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.d("SurveyData", e.getMessage());
        }
        return null;
    }

    /**
     * 保存数据到数据库中
     *
     * @param data
     */
    public void saveSurveyData(SurveyData data) {
        if (data == null)
            return;
        if (data.getMapScene() == null) {
            MapScene mapScene = MapApplication.instance().getLayersManager().getCurrentScene();
            data.setMapScene(mapScene);
        }
        if (!data.isSaved())
            data.save();
        surveyDataMap.put(data.getBaseObjId(), data);
    }

    /**
     * 如果数据修改，更新新的修改到数据库中
     */
    public void updateSurveyData(SurveyData surveyData, Geometry geometry) {
        if (surveyData == null || geometry == null)
            return;
        if (!surveyData.isSaved())
            return;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos));
            oos.writeObject(geometry);
            oos.close();
            byte[] bytes = bos.toByteArray();
            bos.close();
            surveyData.setGeometry(bytes);
        } catch (StreamCorruptedException e) {
            Log.d("SurveyData", e.getMessage());
        } catch (IOException e) {
            Log.d("SurveyData", e.getMessage());
        }
        surveyData.update(surveyData.getBaseObjId());
    }

    public void updateSurveyData(SurveyData surveyData, Symbol symbol) {
        if (surveyData == null || symbol == null)
            return;
        if (!surveyData.isSaved())
            return;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos));
            bos.reset();
            oos.reset();
            oos.writeObject(symbol);
            oos.close();
            byte[] bytes = bos.toByteArray();
            bos.close();
            surveyData.setSymbolStyle(bytes);
        } catch (StreamCorruptedException e) {
            Log.d("SurveyData", e.getMessage());
        } catch (IOException e) {
            Log.d("SurveyData", e.getMessage());
        }
        surveyData.update(surveyData.getBaseObjId());
    }

    public void updateSurveyData(SurveyData surveyData, Map<String, Object> attributes) {
        if (surveyData == null || attributes == null)
            return;
        if (!surveyData.isSaved())
            return;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos));
            bos.reset();
            oos.reset();
            oos.writeObject(attributes);
            oos.close();
            byte[] bytes = bos.toByteArray();
            bos.close();
            surveyData.setAttributes(bytes);
        } catch (StreamCorruptedException e) {
            Log.d("SurveyData", e.getMessage());
        } catch (IOException e) {
            Log.d("SurveyData", e.getMessage());
        }
        surveyData.update(surveyData.getBaseObjId());
    }

    private MapScene getCurrentScene(){
        MapScene mapScene = MapApplication.instance().getLayersManager().getCurrentScene();
        return mapScene;
    }

    /**
     * 加载当前场景相关的地图对象
     *
     * @return
     */
    public List<SurveyData> loadSurveyDataSet(int geoType) {
        MapScene mapScene = getCurrentScene();
        int sceneId = mapScene.getId();
        List<SurveyData> dbSurveyDataList = DataSupport.where("mapScene_id = ? and geoType = ?",
                String.valueOf(sceneId), String.valueOf(geoType)).find(SurveyData.class);
        if (dbSurveyDataList == null)
            return null;
        List<SurveyData> newDataList = new ArrayList<>(dbSurveyDataList.size());
        for (SurveyData surveyData : dbSurveyDataList) {
            if (!surveyDataMap.containsKey(surveyData.getBaseObjId())) {
                surveyDataMap.put(surveyData.getBaseObjId(), surveyData);
                surveyData.setMapScene(mapScene);
                newDataList.add(surveyData);
            }
        }
        return newDataList;
    }

    /**
     * 清空所有已经加载缓存的临时数据
     */
    public void clearSurveyDataSet() {
        surveyDataMap.clear();
    }

    /**
     * 导出所有数据到指定目录
     */
    public void exportAllSurveyData(){
        exportSurveyData(SurveyDataLayer.GeoType.POINT.value());
        exportSurveyData(SurveyDataLayer.GeoType.POLYLINE.value());
        exportSurveyData(SurveyDataLayer.GeoType.POLYGON.value());
    }

    /**
     * 将采集数据填充到OGR的格式中
     *
     * @param data
     * @param oFeature
     */
    private boolean surveyDataToOgr(SurveyData data, Feature oFeature) {
        Graphic graphic = fromSurveyData(data);
        if(graphic == null )
            return false;
        //转换属性信息
        Map<String, Object> attributes = graphic.getAttributes();
        for (Map.Entry<String, Integer> entry : surveyFields.entrySet()) {
            int ogrFieldType = esriToOgrFieldType.get(entry.getValue());
            String fieldName = entry.getKey();
            if (!attributes.containsKey(fieldName))
                continue;
            Object value = attributes.get(fieldName);
            if (value == null)
                continue;
            switch (ogrFieldType) {
                case ogr.OFTString:
                    oFeature.SetField(fieldName, value.toString());
                    break;
                case ogr.OFTInteger:
                    try {
                        int intVal = Integer.parseInt(value.toString());
                        oFeature.SetField(fieldName, intVal);
                    } catch (NumberFormatException e) {
                        System.out.print(e.toString());
                    }
                    break;
                case ogr.OFTReal:
                    try {
                        double dbValue = Double.parseDouble(value.toString());
                        oFeature.SetField(fieldName, dbValue);
                    } catch (NumberFormatException e) {
                        System.out.print(e.toString());
                    }
                    break;
                case ogr.OFTDateTime:
                    Date date = null;
                    if (value instanceof Date) {
                        date = (Date) value;
                    } else {
                        try {
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            date = formatter.parse(value.toString());
                        } catch (ParseException e) {
                            System.out.print(e.toString());
                        }
                    }
                    if (date != null) {
                        oFeature.SetField(fieldName, date.getYear(), date.getMonth(), date.getDay(),
                                date.getHours(), date.getMinutes(), date.getSeconds(), 8);
                    }
                    break;
//                    case ogr.OFTBinary:
//                        oFeature.
//                        break;
            }
        }
        //转换图形信息  POINT(513),LINE(5122),ENVELOPE(3077),MULTIPOINT(8710),POLYLINE(25607),POLYGON(27656);
        Geometry geometry = graphic.getGeometry();
        String wkt = GeometryUtils.GeometryToWKT(geometry);
        org.gdal.ogr.Geometry geom= org.gdal.ogr.Geometry.CreateFromWkt(wkt);
        if(geom == null )
            return false;
        oFeature.SetGeometry(geom);
        return true;
    }

    /**
     * 导出采集数据(使用shp格式)
     *
     * @param geoType
     */
    public void exportSurveyData(int geoType) {
        MapApplication application = MapApplication.instance();
        LayersManager layersManager = application.getLayersManager();
        MapView mapView = layersManager.getMapView();
        //获得投影坐标系
        com.esri.core.geometry.SpatialReference spatialRef = mapView.getSpatialReference();
        if (spatialRef == null)
            return;
        //导出数据根目录
        String outputPath = application.getOutputPath();
        MapScene mapScene = layersManager.getCurrentScene();
        String sceneName = mapScene.getSceneName();
        File base = new File(outputPath + "/" + sceneName);
        if (!base.exists())
            base.mkdir();
        //导出shp文件名称
        String strVectorFile = base.getAbsolutePath() + "/" + SurveyData.GeoTypeStrings[geoType] + ".shp";
        // 为了支持中文路径，请添加下面这句代码
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "NO");
        // 为了使属性表字段支持中文，请添加下面这句
        gdal.SetConfigOption("SHAPE_ENCODING", "");
        //创建数据，这里以创建ESRI的shp文件为例
        String strDriverName = "ESRI Shapefile";
        org.gdal.ogr.Driver oDriver = ogr.GetDriverByName(strDriverName);
        if (oDriver == null) {
            System.out.println(strVectorFile + " 驱动不可用！\n");
            return;
        }
        DataSource oDS = oDriver.Open(strVectorFile);
        if (oDS != null) {
            oDriver.DeleteDataSource(strVectorFile);
        }
        // 创建数据源
        oDS = oDriver.CreateDataSource(strVectorFile, null);
        if (oDS == null) {
            System.out.println("创建矢量文件【" + strVectorFile + "】失败！\n");
            return;
        }
        String wkt = spatialRef.getText();
        // 创建图层，创建一个多边形图层，这里没有指定空间参考，如果需要的话，需要在这里进行指定
        SpatialReference sr = new SpatialReference(wkt); //osr.SRS_WKT_WGS84
        Layer oLayer = oDS.CreateLayer("TestPolygon", sr, geoType , null);
        if (oLayer == null) {
            System.out.println("图层创建失败！\n");
            return;
        }
        // 下面创建属性表
        for (Map.Entry<String, Integer> entry : surveyFields.entrySet()) {
            int ogrFieldType = esriToOgrFieldType.get(entry.getValue());
            String fieldName = entry.getKey();
            FieldDefn oField = new FieldDefn(fieldName, ogrFieldType);
            oLayer.CreateField(oField, 1);
        }
        //下面填充图形数据和属性值
        FeatureDefn oDefn = oLayer.GetLayerDefn();
        for (Map.Entry<Long, SurveyData> entry : surveyDataMap.entrySet()) {
            SurveyData data = entry.getValue();
            if (entry.getValue().getGeoType() == geoType) {
                Feature oFeature = new Feature(oDefn);
                if(surveyDataToOgr(data, oFeature) )
                    oLayer.CreateFeature(oFeature);
            }
        }
        oDS.SyncToDisk();
//        Feature oFeatureTriangle = new Feature(oDefn);
//        oFeatureTriangle.SetField(0, 0);
//        oFeatureTriangle.SetField(1, "三角形");
//        org.gdal.ogr.Geometry geomTriangle = org.gdal.ogr.Geometry.CreateFromWkt("POLYGON ((0 0,20 0,10 15,0 0))");
//        oFeatureTriangle.SetGeometry(geomTriangle);
//        oLayer.CreateFeature(oFeatureTriangle);
//        // 创建矩形要素
//        Feature oFeatureRectangle = new Feature(oDefn);
//        oFeatureRectangle.SetField(0, 1);
//        oFeatureRectangle.SetField(1, "矩形");
//        org.gdal.ogr.Geometry geomRectangle = org.gdal.ogr.Geometry.CreateFromWkt("POLYGON ((30 0,60 0,60 30,30 30,30 0))");
//        oFeatureRectangle.SetGeometry(geomRectangle);
//        oLayer.CreateFeature(oFeatureRectangle);
//        // 创建五角形要素
//        Feature oFeaturePentagon = new Feature(oDefn);
//        oFeaturePentagon.SetField(0, 2);
//        oFeaturePentagon.SetField(1, "五角形");
//        org.gdal.ogr.Geometry geomPentagon = org.gdal.ogr.Geometry.CreateFromWkt("POLYGON ((70 0,85 0,90 15,80 30,65 15,70 0))");
//        oFeaturePentagon.SetGeometry(geomPentagon);
//        oLayer.CreateFeature(oFeaturePentagon);
//        System.out.println("\n数据集创建完成！\n");
//        oDS.SyncToDisk();
    }

    public void loadAllSurveyData() {
        List<SurveyData> dbSurveyDataList = DataSupport.findAll(SurveyData.class);
        int progressInt = 0;
        for (SurveyData surveyData : dbSurveyDataList) {
            progressInt++;
            Graphic tmp = fromSurveyData(surveyData);
        }
    }

    public void deleteSurveyData(SurveyData data) {
        if (data != null) {
            data.delete();
            surveyDataMap.remove(data.getBaseObjId());
        }
    }

    /**
     * 测试使用代码
     */
    public void deleteAllSurveyData() {
        DataSupport.deleteAll(SurveyData.class);
    }

    /**
     * 测试使用代码
     */
    public void testSave() {
        com.esri.core.geometry.Point point = new com.esri.core.geometry.Point(112, 32);
        com.esri.core.symbol.MarkerSymbol markerSymbol = new SimpleMarkerSymbol(Color.BLACK, 16,
                SimpleMarkerSymbol.STYLE.CIRCLE);
        Graphic graphic = new Graphic(point, markerSymbol);
        SurveyData surveyData = toSurveyData(graphic);
        saveSurveyData(surveyData);
        Graphic tmp = fromSurveyData(surveyData);

        Polygon polygon = new Polygon();
        Line line = new Line();
        line.setStart(new Point(112.321, 23.232));
        line.setEnd(new Point(114.321, 24.232));
        polygon.addSegment(line, false);
        line = new Line();
        line.setStart(new Point(114.321, 24.232));
        line.setEnd(new Point(115.321, 25.232));
        polygon.addSegment(line, false);
        line = new Line();
        line.setStart(new Point(115.321, 25.232));
        line.setEnd(new Point(115.321, 24.67));
        polygon.addSegment(line, false);
        line = new Line();
        line.setStart(new Point(115.321, 23.232));
        line.setEnd(new Point(112.321, 23.232));
        polygon.addSegment(line, false);
        SimpleFillSymbol fillSymbol = new SimpleFillSymbol(Color.BLACK);
        graphic = new Graphic(polygon, fillSymbol);
        surveyData = toSurveyData(graphic);
        saveSurveyData(surveyData);

        loadAllSurveyData();
    }
}
