package com.example.administrator.mapdev;

import org.litepal.crud.DataSupport;

/**
 * Created by caizhihuan on 2016/11/4.
 */
public class MapLayer extends DataSupport {
    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    private String layerName;

}
