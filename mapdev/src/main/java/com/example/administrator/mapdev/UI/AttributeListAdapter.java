package com.example.administrator.mapdev.UI;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.esri.core.map.Feature;
import com.esri.core.map.Field;
import com.example.administrator.mapdev.R;
import com.example.administrator.mapdev.tools.FeatureLayerUtils;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 属性编辑
 * Created by Administrator on 2016/12/5.
 */
public class AttributeListAdapter extends BaseAdapter {
    private List<AttributeItem> items;
    private Feature feature;
    private Context context;
    private LayoutInflater inflater;
    private String TAG = "AttributeListAdapter";
    DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public AttributeListAdapter(Context context, List<Field> fields, Feature feature) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.feature = feature;
        initAttributeItems(fields, feature);
    }

    private void initAttributeItems(List<Field> fields, Feature feature) {
        if (fields == null)
            return;
        if (fields.size() == 0)
            return;
        Map<String, Object> attributes = feature.getAttributes();
        if (attributes == null) {
            //TODO:初始化属性值
            attributes = new LinkedHashMap<>(fields.size());
        }
        items = new ArrayList<>(fields.size());
        for (Field field : fields) {
            //if( !FeatureLayerUtils.isFieldValidForEditing(field) )
            //    continue;
            FeatureLayerUtils.FieldType fieldType = FeatureLayerUtils.FieldType.determineFieldType(field);
            if (fieldType == null)
                continue;
            AttributeItem attributeItem = new AttributeItem();
            attributeItem.setField(field);
            Object value = attributes.get(field.getName()); //TODO: 确认是用Name还是Alias
            //if(fieldType == FeatureLayerUtils.FieldType.STRING){
            //    attributeItem.setValue(decodeUTF8(value));
            //}else
            //    attributeItem.setValue(value);
            attributeItem.setValue(value);
            items.add(attributeItem);
        }
    }

    public List<AttributeItem> getItems() {
        return items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View container = null;
        View valueView = null;
        AttributeItem item = (AttributeItem) getItem(position);
        //判断字段类型 NUMBER, STRING, DECIMAL, DATE
        FeatureLayerUtils.FieldType fieldType = FeatureLayerUtils.FieldType.determineFieldType(item.getField());
        switch (fieldType) {
            case STRING:
                container = inflater.inflate(R.layout.item_text, null);
                valueView = createAttributeRow(container, item.getField(),
                        item.getValue());
                item.setView(valueView);
                break;
            case NUMBER:
                container = inflater.inflate(R.layout.item_number, null);
                valueView = createAttributeRow(container, item.getField(),
                        item.getValue());
                item.setView(valueView);
                break;
            case DECIMAL:
                container = inflater.inflate(R.layout.item_decimal, null);
                valueView = createAttributeRow(container, item.getField(),
                        item.getValue());
                item.setView(valueView);
                break;
            case DATE:
                container = inflater.inflate(R.layout.item_date, null);
                long date = Long.parseLong(item.getValue().toString());
                Button dateButton = createDateButtonFromLongValue(container,
                        item.getField(), date);
                item.setView(dateButton);
                break;
        }
        return container;
    }

    /**
     * Helper method to create a spinner for a field and insert it into the View
     * container. This uses, the String[] to create the list, and selects the
     * value that is passed in from the list (the features value). Can be used
     * for domains as well as types.
     */
    Spinner createSpinnerViewFromArray(View container, Field field,
                                       Object value, String[] values) {
        TextView fieldAlias = (TextView) container
                .findViewById(R.id.field_alias_txt);
        Spinner spinner = (Spinner) container
                .findViewById(R.id.field_value_spinner);
        fieldAlias.setText(field.getAlias());
        spinner.setPrompt(field.getAlias());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                this.context, android.R.layout.simple_spinner_item, values);
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        // set current selection based on the value passed in
        spinner.setSelection(spinnerAdapter.getPosition(value.toString()));
        return spinner;
    }

    /**
     * Helper method to create a date button, with appropriate onClick and
     * onDateSet listeners to handle dates as a long (milliseconds since 1970),
     * it uses the locale and presents a button with the date and time in short
     * format.
     */
    Button createDateButtonFromLongValue(View container, Field field, long date) {
        TextView fieldAlias = (TextView) container
                .findViewById(R.id.field_alias_txt);
        Button dateButton = (Button) container
                .findViewById(R.id.field_date_btn);
        fieldAlias.setText(field.getAlias());
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date);
        dateButton.setText(formatter.format(c.getTime()));
        addListenersToDatebutton(dateButton);
        return dateButton;
    }

    /**
     * Helper method to add the field alias and the fields value into columns of
     * a view using standard id names. If the field has a length set, then this
     * is used to constrain the EditText's allowable characters. No validation
     * is applied here, it is assumed that the container has this set already
     * (in XML).
     */
    View createAttributeRow(View container, Field field, Object value) {
        TextView fieldAlias = (TextView) container
                .findViewById(R.id.field_alias_txt);
        EditText fieldValue = (EditText) container
                .findViewById(R.id.field_value_txt);
        fieldAlias.setText(field.getName());
        // set the length of the text field and its value
        if (field.getLength() > 0) {
            InputFilter.LengthFilter filter = new InputFilter.LengthFilter(
                    field.getLength());
            fieldValue.setFilters(new InputFilter[]{filter});
        }
        Log.d(TAG, "value is null? =" + (value == null));
        Log.d(TAG, "value=" + value);
        if (value != null) {
            fieldValue.setText(value.toString(), TextView.BufferType.EDITABLE);
        } else {
            fieldValue.setText("", TextView.BufferType.EDITABLE);
        }
        return fieldValue;
    }

    /**
     * Helper method to create the date button and its associated events
     */
    void addListenersToDatebutton(Button dateButton) {

        // create new onDateSetLisetener with the button associated with it
        final ListOnDateSetListener listener = new ListOnDateSetListener(
                dateButton);
        // add a click listener to the button
        dateButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // if its a date, get the milliseconds value
                Calendar c = Calendar.getInstance();
                formatter.setCalendar(c);
                try {
                    // parse to a double
                    Button button = (Button) v;
                    c.setTime(formatter.parse(button.getText().toString()));
                } catch (ParseException e) {
                    // do nothing as should parse
                }
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                // show date picker with date set to the items value (hence
                // built
                // outside of onCreateDialog)
                // TODO implement time picker if required, this picker only
                // supports
                // date and therefore showing the dialog will cause a change in
                // the time
                // value for the field
                DatePickerDialog dialog = new DatePickerDialog(context,
                        listener, year, month, day);
                dialog.show();
            }
        });
    }

    /**
     * 更新界面修改值到属性中
     */
    public Map<String, Object> updateAttributes() {
        Map<String, Object> attributes = feature.getAttributes();
        if (attributes == null)
            return attributes;
        for (AttributeItem item : items) {
            TextView textView = (TextView) item.getView();
            if (textView == null)
                continue;
            if (textView.getText() == null)
                continue;
            switch (item.getField().getFieldType()) {
                case Field.esriFieldTypeString:
                    item.setValue(textView.getText().toString());
                    attributes.put(item.getField().getName(), item.getValue());
                    break;
                case Field.esriFieldTypeSmallInteger:
                    try {
                        item.setValue(Integer.valueOf(textView.getText().toString()));
                        attributes.put(item.getField().getName(), item.getValue());
                    } catch (NumberFormatException e) {
                        Log.d(TAG, e.getMessage());
                    }
                    break;
                case Field.esriFieldTypeInteger:
                    try {
                        item.setValue(Long.valueOf(textView.getText().toString()));
                        attributes.put(item.getField().getName(), item.getValue());
                    } catch (NumberFormatException e) {
                        Log.d(TAG, e.getMessage());
                    }
                    break;
                case Field.esriFieldTypeSingle:
                    try {
                        item.setValue(Float.valueOf(textView.getText().toString()));
                        attributes.put(item.getField().getName(), item.getValue());
                    } catch (NumberFormatException e) {
                        Log.d(TAG, e.getMessage());
                    }
                    break;
                case Field.esriFieldTypeDouble:
                    try {
                        item.setValue(Double.valueOf(textView.getText().toString()));
                        attributes.put(item.getField().getName(), item.getValue());
                    } catch (NumberFormatException e) {
                        Log.d(TAG, e.getMessage());
                    }
                    break;
                case Field.esriFieldTypeDate:
                    try {
                        Date date = formatter.parse(textView.getText().toString());
                        item.setValue(date);
                        attributes.put(item.getField().getName(), item.getValue());
                    } catch (ParseException e) {
                        Log.d(TAG, e.getMessage());
                    }
                    break;
            }
        }
        return attributes;
    }

    private Object decodeUTF8(Object value) {
        if (value == null)
            return null;
        if (value instanceof String) {
            String valueString = (String) value;
            String encoding = getEncoding(valueString);
            try {
                String encodeString = new String(valueString.getBytes(encoding), "UTF-8");
                return encodeString;
            } catch (UnsupportedEncodingException e) {
                Log.d(TAG, e.getMessage());
            }
        }
        return value;
    }

    public static String getEncoding(String str) {
        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s = encode;
                return s;
            }
        } catch (Exception exception) {
        }
        encode = "ISO-8859-1";
        try {

            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s1 = encode;
                return s1;
            }
        } catch (Exception exception1) {
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s2 = encode;
                return s2;
            }
        } catch (Exception exception2) {
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s3 = encode;
                return s3;
            }
        } catch (Exception exception3) {
        }
        return "";
    }

    /**
     * Inner class for handling date change events from the date picker dialog
     */
    class ListOnDateSetListener implements DatePickerDialog.OnDateSetListener {

        Button button;

        public ListOnDateSetListener(Button button) {
            this.button = button;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            // Update the button to show the chosen date
            button.setText(formatter.format(c.getTime()));
        }
    }

}
