package com.example.administrator.mapdev.Action;

import android.content.Context;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.esri.android.map.MapView;
import com.example.administrator.mapdev.LayersManager;
import com.example.administrator.mapdev.MapApplication;
import com.example.administrator.mapdev.R;
import com.example.administrator.mapdev.UI.AttributeDialog;
import com.example.administrator.mapdev.tools.AttributeEditorTool;

/**
 * 属性编辑类
 * Created by Administrator on 2016/11/19.
 */
public class AttributeEditorAction implements ActionMode.Callback {
    private MapView mapView;
    private LayersManager layersManager;
    private AttributeEditorTool attributeEditorTool;
    public AttributeEditorAction(){
        layersManager = MapApplication.instance().getLayersManager();
        mapView = layersManager.getMapView();
        attributeEditorTool = new AttributeEditorTool(layersManager);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater menuInflater = mode.getMenuInflater();
        menuInflater.inflate(R.menu.menu_editor_tool, menu);

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        attributeEditorTool.deactivate();
        switch (item.getItemId()){
            case R.id.geometry_editor:
                break;
            case R.id.attribute_editor:
                attributeEditorTool.activate();
                break;
            case R.id.undo_editor:
                break;
            case R.id.redo_editor:
                break;
            case R.id.geometry_delete:
                break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

}
