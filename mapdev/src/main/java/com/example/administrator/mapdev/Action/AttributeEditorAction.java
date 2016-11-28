package com.example.administrator.mapdev.Action;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureServiceTable;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geodatabase.ShapefileFeature;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.map.Feature;
import com.esri.core.table.FeatureTable;
import com.esri.core.table.TableException;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * 属性编辑类
 * Created by Administrator on 2016/11/19.
 */
public class AttributeEditorAction implements ActionMode.Callback {

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
