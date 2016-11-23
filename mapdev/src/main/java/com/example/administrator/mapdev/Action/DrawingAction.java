package com.example.administrator.mapdev.Action;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

/**
 * 绘制类
 * Created by Administrator on 2016/11/19.
 */
public class DrawingAction implements ActionMode.Callback {

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
