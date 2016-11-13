package com.example.administrator.mapdev;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
        View view = inflater.inflate(R.layout.fragment_scene, container, false);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.scene_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed();
            }
        });
        Button okButton = (Button) view.findViewById(R.id.ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( saveMapScene() )
                    onButtonPressed();
            }
        });
        Button cancelButton = (Button) view.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed();
            }
        });
        final EditText baseRasterPathET = (EditText) view.findViewById(R.id.baseRasterPath);
        ImageButton button=(ImageButton)view.findViewById(R.id.openRasterPath);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String suffix = ".tiff;.tif;.img;";
                String currentPath = MapApplication.instance().getDataPath();
//                FileBrowserDialog dialog = FileBrowserDialog.newInstance(0, suffix, currentPath);
//                dialog.show(getFragmentManager(), "loginDialog");
//                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);
//                builder.setTitle("这里是Title");
//                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(MainActivity.this, editText.getText().toString(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//                LayoutInflater inflater = (LayoutInflater) getContext()
//                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                View dialog = inflater.inflate(R.layout.dialog_file_browser,null);
//                FileSelectView fileView = (FileSelectView)dialog.findViewById(R.id.fileListView);
//                fileView.setPath(currentPath);
//                fileView.setSuffix(suffix);
//                fileView.refreshFileList();
//                builder.setView(dialog);
//                builder.setIcon(R.mipmap.ic_launcher);
//                builder.show();
                FileBrowserDialog dialog = new FileBrowserDialog(getContext());
                dialog.setSuffix(suffix);
                dialog.setInitialDir(currentPath);
                dialog.setYesOnclickListener(new FileBrowserDialog.onYesOnclickListener() {
                    @Override
                    public boolean onYesClick(int action_type, String path) {
                        if (action_type == FileBrowserDialog.ACTION_FILE) {
                            baseRasterPathET.setText(path);
                            return true;
                        } else
                            return false;
                    }
                });
                dialog.show();
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
    private boolean saveMapScene() {
        View view = getView();
        EditText mapNameET = (EditText) view.findViewById(R.id.mapName);
        String sceneName = mapNameET.getText().toString();
        if (sceneName.length() < 1) {
            MapApplication.showMessage("地图名称不能为空");
            return false;
        }
        EditText mapUserET = (EditText) view.findViewById(R.id.mapUser);
        String mapUser = mapUserET.getText().toString();
        if (mapUser.length() < 1) {
            MapApplication.showMessage("用户名称不能为空");
            return false;
        }
        int count = DataSupport.select("sceneName").where("sceneName = ?", sceneName).count(MapScene.class);
        if (count > 0) {
            MapApplication.showMessage("地图已经存在");
            return false;
        }
        EditText baseRasterPathET = (EditText) view.findViewById(R.id.baseRasterPath);
        String baseRasterPath = baseRasterPathET.getText().toString();
        if (baseRasterPath.length() < 1) {
            MapApplication.showMessage("需要选择一张影像作为底图");
            return false;
        }
        EditText mapDescriptionET = (EditText) view.findViewById(R.id.mapDescription);
        String mapDescription = mapDescriptionET.getText().toString();

        Date date = new Date();
        MapScene mapScene = new MapScene();
        mapScene.setSceneName(sceneName);
        mapScene.setUserName(mapUser);
        mapScene.setDescription(mapDescription);
        mapScene.setLastOpenDate(date);
        mapScene.setCreateDate(date);
        mapScene.setBaseRasterPath(baseRasterPath);
        if (mapScene.save())
        {
            //保存影像底图到图层数据库中
            mapScene.saveBaseRasterLayer();
            mapSceneManager.setCurrentScene(mapScene);
            return true;
        }else{
            Toast.makeText(MapApplication.getContext(), "地图保存失败", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}
