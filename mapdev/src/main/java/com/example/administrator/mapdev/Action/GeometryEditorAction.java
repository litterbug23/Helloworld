package com.example.administrator.mapdev.Action;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.administrator.mapdev.R;

/**
 * 编辑菜单处理
 * Created by caizhihuan on 2016/11/17.
 */
public class GeometryEditorAction implements ActionMode.Callback {
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater menuInflater = mode.getMenuInflater();
        menuInflater.inflate(R.menu.menu_survey_tool,menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch(item.getItemId()){
            case R.id.geometry_editor:
                break;
            case R.id.attribute_editor:
                break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }
}
