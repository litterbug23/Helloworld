package com.example.administrator.mapdev.UI;

import android.view.View;

import com.esri.core.map.Field;

/**
 * 属性编辑对话框设置项
 * Created by Administrator on 2016/12/5.
 */
public class AttributeItem {

    private Field field;

    private Object value;

    private View view;

    public View getView() {

        return view;
    }

    public void setView(View view) {

        this.view = view;
    }

    public Field getField() {

        return field;
    }

    public void setField(Field field) {

        this.field = field;
    }

    public Object getValue() {

        return value;
    }

    public void setValue(Object value) {

        this.value = value;
    }
}
