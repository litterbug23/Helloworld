package com.example.administrator.mapdev.Action;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.administrator.mapdev.LayersManager;
import com.example.administrator.mapdev.MapApplication;
import com.example.administrator.mapdev.R;
import com.example.administrator.mapdev.tools.DrawTool;

/**
 * 绘制类
 * Created by Administrator on 2016/11/19.
 */
public class DrawingAction implements ActionMode.Callback {

    private DrawTool drawTool;

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater menuInflater = mode.getMenuInflater();
        menuInflater.inflate(R.menu.menu_draw_tool,menu);
        LayersManager layersManager = MapApplication.instance().getLayersManager();
        drawTool = new DrawTool(layersManager);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_label:
                drawTool.activate(DrawTool.POINT);
                break;
            case R.id.add_polyline:
                drawTool.activate(DrawTool.POLYLINE);
                break;
            case R.id.add_polygon:
                drawTool.activate(DrawTool.POLYGON);
                break;
            case R.id.free_hand:
                drawTool.activate(DrawTool.FREEHAND_POLYGON);
                break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        drawTool.deactivate();
    }
}
