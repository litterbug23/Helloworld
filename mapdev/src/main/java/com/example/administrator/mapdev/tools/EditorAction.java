package com.example.administrator.mapdev.tools;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

/**
 * 编辑菜单处理
 * Created by caizhihuan on 2016/11/17.
 */
public class EditorAction implements ActionMode.Callback {
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }
}
