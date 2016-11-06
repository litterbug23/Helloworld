package com.example.administrator.mapdev;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/6.
 */
public class MapSceneManager {
    private MapScene currentScene;

    public MapSceneManager() {

    }



    /**
     *打开场景
     * @param sceneName 场景名称
     * @return
     */
    public MapScene loadMapScene(String sceneName){
        List<MapScene> mapScenes = DataSupport.where("sceneName = ?",sceneName).find(MapScene.class);
        if(mapScenes == null || mapScenes.size()<1 )
            return null;
        currentScene=mapScenes.get(0);

        return currentScene;
    }

    /**
     * 加载所有图层的名称，并且按照最后的打开时间进行排序
     * @return
     */
    public List<String> loadMapSceneNames(){
        List<MapScene> mapScenes = DataSupport.select("sceneName").order("lastOpenDate desc").find(MapScene.class);
        List<String> mapSceneNames = new ArrayList<>();
        for (MapScene mapscene: mapScenes ) {
            mapSceneNames.add(mapscene.getSceneName());
        }
        return mapSceneNames;
    }

    public List<LayerItemData> loadRasterLayers()
    {
        currentScene.getMapLayers();
    }



}
