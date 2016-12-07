package com.example.administrator.mapdev;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by caizhihuan on 2016/11/4.
 * 地图工程管理类
 * 每次出去作外业数据采集，可能都会重新再型建外业数据的类
 */
public class MapScene extends DataSupport {

    static public String wktXiAn80 = "PROJCS[\"Xian_1980_3_Degree_GK_Zone_39\",GEOGCS[\"GCS_Xian_1980\",DATUM[\"D_Xian_1980\",SPHEROID[\"Xian_1980\",6378140.0,298.257]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Gauss_Kruger\"],PARAMETER[\"False_Easting\",39500000.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",117.0],PARAMETER[\"Scale_Factor\",1.0],PARAMETER[\"Latitude_Of_Origin\",0.0],UNIT[\"Meter\",1.0]]";
    static public String wktWGS84 = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9108\"]],AUTHORITY[\"EPSG\",\"4326\"]]";
    static public String wktGCS2000 = "GEOGCS[\"GCS_China_Geodetic_Coordinate_System_2000\",DATUM[\"D_China_2000\",SPHEROID[\"CGCS2000\",6378137.0,298.257222101]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]]";

    @Column(unique = true)
    private int id;
    @Column(unique = true)
    private String sceneName;           //地图名称
    @Column(nullable = false)
    private String userName;            //创建地图用户
    private String description;         //地图描述信息
    private String wktExt = wktXiAn80;  //地图投影
    private String baseRasterPath;      //影像底图路径
    private Date createDate;            //地图创建日期
    private Date lastOpenDate;          //最后一次打开时间
    private double calibrationLong;     //校正经度
    private double calibrationLat;      //校正纬度
    private List<LayerItemData> mapLayers = new ArrayList<>();  //LayerItem 与 MapScene建立表关联
    private List<SurveyData> surveyDatas = new ArrayList<>();    //SurveyData 与 MapScene建立表关联
    private List<PhotoSurvey> photoSurveys= new ArrayList<>();   //PhotoSurvey 与 MapScene建立表关联

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getWktExt() {
        return wktExt;
    }

    public void setWktExt(String wktExt) {
        this.wktExt = wktExt;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastOpenDate() {
        return lastOpenDate;
    }

    public void setLastOpenDate(Date lastOpenDate) {
        this.lastOpenDate = lastOpenDate;
    }

    public void setBaseRasterPath(String baseRasterPath) {
        this.baseRasterPath = baseRasterPath;
    }

    public String getBaseRasterPath() {
        return baseRasterPath;
    }

    public void setCalibrationLong(double calibrationLong) {  this.calibrationLong=calibrationLong; }

    public double getCalibrationLong() {return calibrationLong; }

    public void setCalibrationLat(double calibrationLat) {  this.calibrationLat=calibrationLat; }

    public double getCalibrationLat() {return calibrationLat; }

    public List<LayerItemData> getMapLayers() {
        return mapLayers;
    }

    public void setMapLayers(List<LayerItemData> mapLayers) {
        this.mapLayers = mapLayers;
    }

    public List<SurveyData> getSurveyDatas() {
        return surveyDatas;
    }

    public void setSurveyDatas(List<SurveyData> surveyDatas) {
        this.surveyDatas = surveyDatas;
    }

    public List<PhotoSurvey> getPhotoSurveys() {
        return photoSurveys;
    }

    public void setPhotoSurveys(List<PhotoSurvey> photoSurveys) {
        this.photoSurveys = photoSurveys;
    }

    /**
     * 按照orderID进行排序的列表
     *当地图加载时，LayerManager会调用此函数一次
     * @return
     */
    public List<LayerItemData> getOrderMapLayers() {
        //saveBaseRasterLayer();
        //图层类型按照倒序排序，orderID按照升序排序，确保底层图层优先被加载
        List<LayerItemData> layerItems = DataSupport.where("MapScene_id = ?",
                String.valueOf(getId())).order("layerType desc,orderId asc").find(LayerItemData.class);
        for(LayerItemData item : layerItems ){
            item.setMapScene(this);
        }
        return layerItems;
    }

    /**
     * 场景中是否已经加入过此数据源
     * @param dataSource 基础影像图层路径
     * @return
     */
    public boolean hasDataSource(String dataSource) {
        int result = DataSupport.where("MapScene_id = ? and dataSource = ?",
                String.valueOf(getId()), dataSource).count(LayerItemData.class);
        if (result > 0)
            return true;
        return false;
    }

    /**
     * 保存基础图层信息
     * @return 判断基础图层是否已经正确加入
     */
    public boolean saveBaseRasterLayer() {
        if ( !hasDataSource(baseRasterPath) ) {
            LayerItemData layerItemData=new LayerItemData();
            layerItemData.setGeometryType(LayerItemData.RASTER);
            layerItemData.setDataSource(baseRasterPath);
            layerItemData.setLayerType(LayerItemData.RASTER_LAYER);
            layerItemData.setOrderId(0);
            layerItemData.setMapScene(this);
            return layerItemData.save();
        }
        return true;
    }
}
