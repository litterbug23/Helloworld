package com.example.administrator.mapdev.Action;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.administrator.mapdev.R;
import com.example.administrator.mapdev.SurveyDataCaptureTool;

/**
 * 采集数据绘制保存
 * Created by Administrator on 2016/11/19.
 */
public class SurveyDataCaptureAction implements ActionMode.Callback {

    private SurveyDataCaptureTool surveyDataCaptureTool;

    public  SurveyDataCaptureAction(){
        surveyDataCaptureTool = new SurveyDataCaptureTool();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater menuInflater = mode.getMenuInflater();
        menuInflater.inflate(R.menu.menu_survey_tool,menu);
        surveyDataCaptureTool.initSurveyDataCaptureTool();
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.gps_position_survey:
                surveyDataCaptureTool.doCaptureGPSLocation();
                break;
            case R.id.draw_position_survey:
                surveyDataCaptureTool.doCaptureByMapTouch();
                break;
            case R.id.undo_position_survey:
                surveyDataCaptureTool.undo();
                break;
            case R.id.redo_position_survey:
                surveyDataCaptureTool.redo();
                break;
            case R.id.end_position_survey:
                surveyDataCaptureTool.doComplete();
                break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        surveyDataCaptureTool.unInitSurveyDataCaptureTool();
    }
}
