package com.example.administrator.mapdev.Action;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureServiceTable;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geodatabase.ShapefileFeature;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.map.Feature;
import com.esri.core.table.FeatureTable;
import com.esri.core.table.TableException;
import com.example.administrator.mapdev.R;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * 属性编辑类
 * Created by Administrator on 2016/11/19.
 */
public class AttributeEditorAction implements ActionMode.Callback {

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater menuInflater = mode.getMenuInflater();
        menuInflater.inflate(R.menu.menu_editor_tool,menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()){
            case R.id.geometry_editor:
                break;
            case R.id.attribute_editor:
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
