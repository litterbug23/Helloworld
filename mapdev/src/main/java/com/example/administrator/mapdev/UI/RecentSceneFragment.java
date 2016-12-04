package com.example.administrator.mapdev.UI;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.administrator.mapdev.MapApplication;
import com.example.administrator.mapdev.MapScene;
import com.example.administrator.mapdev.MapSceneManager;
import com.example.administrator.mapdev.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecentSceneFragment extends Fragment {
    ArrayList<Map<String,Object>> mData= new ArrayList<>();
    MapSceneManager mapSceneManager;

    public RecentSceneFragment() {
        // Required empty public constructor
        mapSceneManager = MapApplication.instance().getLayersManager();
    }

    public static RecentSceneFragment newInstance() {
        RecentSceneFragment fragment = new RecentSceneFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_recent_scene, container, false);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.recent_scene_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadRecentScenes();
    }

    public void onButtonPressed() {
        getFragmentManager().popBackStack();
    }

    private void loadRecentScenes(){
        View view = getView();
        ListView listView =(ListView) view.findViewById(R.id.recent_scene_listView);
        List<MapScene> mapScenes = mapSceneManager.loadMapScenes();
        if(mapScenes.size()<0)
            return ;
        for(MapScene mapScene:mapScenes) {
            Map<String,Object> item = new HashMap<>();
            item.put("title", mapScene.getSceneName());
            item.put("text",mapScene.getUserName()+ " " + mapScene.getDescription());
            mData.add(item);
        }
        SimpleAdapter adapter = new SimpleAdapter(MapApplication.getContext(),mData,R.layout.simple_adapter_list_item,
                new String[]{"title","text"},new int[]{R.id.header_title,R.id.content_text});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String,Object> item =mData.get(position);
                String sceneName = (String) item.get("title") ;
                onButtonPressed();
                mapSceneManager.openMapScene(sceneName);
            }
        });
    }

}
