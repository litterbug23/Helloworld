package com.example.administrator.mapdev.tools;

import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.example.administrator.mapdev.LayersManager;

/**
 * Created by Administrator on 2016/4/24.
 */
public abstract class BaseTool {
    protected MapView mapView;
    protected LayersManager layersManager;
    protected boolean active = false;
    protected int toolType = -1;
    protected MapOnTouchListener mapOnTouchListener;
    protected MapOnTouchListener defaultTouchListener;

    public BaseTool(LayersManager layersManager) {
        //super(MapApplication.getContext(), layersManager.getMapView());
        this.layersManager = layersManager;
        this.mapView = layersManager.getMapView();
    }

    public boolean isActive() {
        return active;
    }

    public int getToolType() {
        return toolType;
    }

    /**
     * 激活当前工作
     * @param toolType 传入工具内部参数
     */
    public void activate(int toolType) {
        if (mapView == null)
            return;
        active = true;
        this.toolType = toolType;
        this.mapView.setOnTouchListener(mapOnTouchListener);
    }

    /**
     * 撤销激活当前工具
     */
    public void deactivate() {
        active = false;
        this.toolType = -1;
        this.mapView.setOnTouchListener(defaultTouchListener);
    }


}
