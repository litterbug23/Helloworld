package com.example.administrator.mapdev.tools;

import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.example.administrator.mapdev.LayersManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/24.
 */
public class ToolsManager {

    private BaseTool currentTool = null;
    private LayersManager layersManager;
    private MapOnTouchListener defaultListener;
    private MapView mapView;
    private Map<Type,BaseTool> tools= new HashMap<>();

    public ToolsManager(LayersManager layersManager) {
        this.layersManager = layersManager;
        this.mapView = layersManager.getMapView();
        defaultListener = new MapOnTouchListener(this.mapView.getContext(),
                this.mapView);
    }

    public BaseTool getCurrentTool() {
        return currentTool;
    }

    public void setCurrentTool(BaseTool tool){
        if(tool == currentTool) {
            return ;
        }
        if(currentTool != null)
            currentTool.deactivate();
        currentTool=tool;
    }

    public void setCurrentTool(Type toolName){
        if( tools.containsKey(toolName) ) {
            BaseTool tool =tools.get(toolName);
            setCurrentTool(tool);
        }
    }

    public void reset(){
        this.mapView.setOnTouchListener(defaultListener);
    }

    public void registerTool(Type toolName, BaseTool tool){
        tool.defaultTouchListener=defaultListener;
        tools.put(toolName,tool);
    }
}
