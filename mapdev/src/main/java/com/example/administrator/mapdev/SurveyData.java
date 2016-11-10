package com.example.administrator.mapdev;

import com.esri.core.map.Graphic;
import com.esri.core.geometry.Geometry;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * Created by Administrator on 2016/4/22.
 */
public class SurveyData extends DataSupport {

    public static String UNKNOW_TYPE = "未知类型"; //"FeatureLayer";
    public static String POINT_TYPE = "采集点数据"; //"FeatureLayer";
    public static String POLYLINE_TYPE = "采集线数据";//"GraphicLayer";
    public static String POLYGON_TYPE = "采集面数据";//"RasterLayer";
    public static String[] GeoTypeStrings = {UNKNOW_TYPE, POINT_TYPE, POLYLINE_TYPE, POLYGON_TYPE};
    @Column(unique = true)
    private int id;
    private int drawOrder=0;
    private int WKID;
    private Date surveyDate=new Date();
    private String description;
    //	private String staff;
    private int geoType;
    private byte[] symbolStyle;
    private byte[] geometry;
    private byte[] attributes;
    private MapScene mapScene;
    protected Graphic graphic;

    @Override
    public long getBaseObjId() {
        return super.getBaseObjId();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDrawOrder() {
        return drawOrder;
    }

    public SurveyData setDrawOrder(int drawOrder) {
        this.drawOrder = drawOrder;
        return this;
    }

    public MapScene getMapScene() {
        return mapScene;
    }

    public void setMapScene(MapScene mapScene) {
        this.mapScene = mapScene;
    }

    public int getWKID() {
        return WKID;
    }

    public SurveyData setWKID(int WKID) {
        this.WKID = WKID;
        return this;
    }

    public Date getSurveyDate() {
        return surveyDate;
    }

    public SurveyData setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public SurveyData setDescription(String description) {
        this.description = description;
        return this;
    }

//	public String getStaff() {
//		return staff;
//	}
//
//	public void setStaff(String staff) {
//		this.staff = staff;
//	}


    /**
     * geometryType 包含三个类型值 0 表示点 1表示线 2表示面
     *
     * @return
     */
    public int getGeoType() {
        return geoType;
    }

    public void setGeoType(int geoType) {
        this.geoType = geoType;
    }

    public byte[] getSymbolStyle() {
        return symbolStyle;
    }

    public SurveyData setSymbolStyle(byte[] symbolStyle) {
        this.symbolStyle = symbolStyle;
        return this;
    }

    public byte[] getGeometry() {
        return geometry;
    }

    public SurveyData setGeometry(byte[] geometry) {
        this.geometry = geometry;
        return this;
    }

    public byte[] getAttributes() {
        return attributes;
    }

    public SurveyData setAttributes(byte[] attributes) {
        this.attributes = attributes;
        return this;
    }

    public Graphic getGraphic() {
        return graphic;
    }

    public void setGraphic(Graphic graphic) {
        this.graphic = graphic;
    }

    static public int getGeoType(Geometry geometry) {
        if (geometry != null) {
            if (geometry.getType() == Geometry.Type.POINT || geometry.getType() == Geometry.Type.MULTIPOINT)
                return 1;
            else if (geometry.getType() == Geometry.Type.LINE || geometry.getType() == Geometry.Type.POLYLINE)
                return 2;
            else if (geometry.getType() == Geometry.Type.ENVELOPE || geometry.getType() == Geometry.Type.POLYGON)
                return 3;
        }
        return 0;
    }
}
