package com.example.administrator.mapdev;

import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.List;

/**
 * Created by caizhihuan on 2016/11/4.
 * 地图工程管理类
 * 每次出去作外业数据采集，可能都会重新再建外业数据的类型
 */
public class MapScene extends DataSupport {
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

    public List<MapLayer> getMapLayers() {
        return mapLayers;
    }

    public void setMapLayers(List<MapLayer> mapLayers) {
        this.mapLayers = mapLayers;
    }

    private String sceneName;
    private String userName;
    private String wktExt;
    private Date createDate;
    private List<MapLayer> mapLayers;
}
