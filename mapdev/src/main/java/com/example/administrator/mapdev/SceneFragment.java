package com.example.administrator.mapdev;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class SceneFragment extends Fragment {

    MapSceneManager mapSceneManager;

    public SceneFragment() {
        // Required empty public constructor
        mapSceneManager = MapApplication.instance().getLayersManager();
    }

    public static SceneFragment newInstance() {
        SceneFragment fragment = new SceneFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_scene, container, false);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.scene_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed();
            }
        });
        Button okButton = (Button)view.findViewById(R.id.ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMapScene();
                onButtonPressed();
            }
        });

        Button cancelButton=(Button)view.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed();
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed() {
        getFragmentManager().popBackStack();
    }

    /**
     * 保存场景地图
     */
    private void saveMapScene(){
        View view = getView();
        EditText mapNameET = (EditText)view.findViewById(R.id.mapName);
        String sceneName=mapNameET.getText().toString();
        if( sceneName.length() <1 ) {
            Toast.makeText(MapApplication.getContext(), "地图名称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        int count = DataSupport.select("sceneName").where("sceneName = ?", sceneName ).count(MapScene.class);
        if(count>0)
        {
            Toast.makeText(MapApplication.getContext(), "地图已经存在", Toast.LENGTH_SHORT).show();
            return;
        }
        EditText mapUserET = (EditText)view.findViewById(R.id.mapUser);
        String mapUser = mapUserET.getText().toString();
        EditText mapDescriptionET = (EditText)view.findViewById(R.id.mapDescription);
        String mapDescription = mapDescriptionET.getText().toString();
        Date date=  new Date();
        MapScene mapScene = new MapScene();
        mapScene.setSceneName(sceneName);
        mapScene.setUserName(mapUser);
        mapScene.setDescription(mapDescription);
        mapScene.setLastOpenDate(date);
        mapScene.setCreateDate(date);
        if( mapScene.save() )
            mapSceneManager.setCurrentScene(mapScene);
    }

}