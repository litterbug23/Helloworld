package com.example.administrator.mapdev;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by Administrator on 2016/11/6.
 */
public abstract class MapSceneManager {

    private MapScene currentScene;
    private OnCurrentMapSceneChangedListener listener;

    public MapSceneManager() {

    }

    public void setCurrentMapSceneChangedListener(OnCurrentMapSceneChangedListener listener){
        this.listener = listener;
    }

    public boolean hasMapScene(){
        if(currentScene == null)
            return false;
        return true;
    }

    public MapScene getCurrentScene() {
        return currentScene;
    }

    public void setCurrentScene(MapScene currentScene) {
        if(this.currentScene != currentScene ){
            MapScene oldScene = this.currentScene;
            this.currentScene = currentScene;
            this.currentScene.setLastOpenDate(new Date());
            this.currentScene.update(this.currentScene.getId());
            if(listener != null )
                listener.onCurrentMapSceneChanged(oldScene,this.currentScene);
            onCurrentMapSceneChanged(oldScene,this.currentScene);
        }
    }

    /**
     *打开场景
     * @param sceneName 场景名称
     * @return
     */
    public MapScene loadMapScene(String sceneName){
        if( hasMapScene() ){
            if( currentScene.getSceneName().equals(sceneName) )
                return currentScene;
        }
        //后面的参数使用true，表示要进行关联查询
        List<MapScene> mapScenes = DataSupport.where("sceneName = ?", sceneName).find(MapScene.class);
        if(mapScenes == null || mapScenes.size()<1 )
            return null;
        MapScene oldScene = this.currentScene;
        this.currentScene = mapScenes.get(0);
        this.currentScene.setLastOpenDate(new Date());
        this.currentScene.update(this.currentScene.getId());
        if(listener != null )
            listener.onCurrentMapSceneChanged(oldScene,this.currentScene);
        onCurrentMapSceneChanged(oldScene,this.currentScene);
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

    public List<MapScene> loadMapScenes() {
        List<MapScene> mapScenes = DataSupport.order("lastOpenDate desc").find(MapScene.class);
        return mapScenes;
    }

    abstract void onCurrentMapSceneChanged(MapScene oldScene,MapScene currentScene);

    public interface OnCurrentMapSceneChangedListener
    {
        void onCurrentMapSceneChanged(MapScene oldScene,MapScene currentScene);
    }
}



