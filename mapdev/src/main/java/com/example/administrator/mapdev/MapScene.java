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

    private int id;
    @Column(unique = true)
    private String sceneName;
    private String userName;
    private String description;
    private String wktExt;
    private Date createDate;
    private Date lastOpenDate;
    private List<LayerItemData> mapLayers = new ArrayList<>();

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

    public List<LayerItemData> getMapLayers() {
        return mapLayers;
    }

    public void setMapLayers(List<LayerItemData> mapLayers) {
        this.mapLayers = mapLayers;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * 按照orderID进行排序的列表
     * @return
     */
    public List<LayerItemData> getOrderMapLayers() {
       return  DataSupport.where( "MapScene_id = ?",
                String.valueOf(getId()) ).order("layerType asc,orderId asc").find(LayerItemData.class);
    }
}
