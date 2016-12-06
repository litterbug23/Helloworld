package com.example.administrator.mapdev.UI;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.esri.core.map.Feature;
import com.esri.core.map.Field;
import com.esri.core.map.Graphic;
import com.example.administrator.mapdev.MapApplication;
import com.example.administrator.mapdev.R;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/12/4.
 */
public class AttributeDialog  extends Dialog {
    private Feature feature;
    private List<Field> fields;
    private AttributeListAdapter attributeListAdapter;

    public AttributeDialog(Context context) {
        super(context);
    }

    public AttributeDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器

    /**
     * 设置取消按钮的显示内容和监听
     *
     * @param onNoOnclickListener
     */
    public void setNoOnclickListener(onNoOnclickListener onNoOnclickListener) {
        this.noOnclickListener = onNoOnclickListener;
    }

    /**
     * 设置确定按钮的显示内容和监听
     *
     * @param onYesOnclickListener
     */
    public void setYesOnclickListener(onYesOnclickListener onYesOnclickListener) {
        this.yesOnclickListener = onYesOnclickListener;
    }

    /**
     * 设置要显示的对话框的属性信息
     * @param feature
     */
    public void setAttribute(List<Field> fields , Feature feature){
        if(feature==null){
            MapApplication.showMessage("未能选择要素对象");
        }
        this.feature=feature;
        this.fields = fields;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = createView(inflater, null, savedInstanceState);
        super.setContentView(contentView);
    }

    private View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_attribute, container, false);
        initEvent(view);
        attributeListAdapter = new AttributeListAdapter(getContext(),
                this.fields,this.feature);
        ListView listView = (ListView) view.findViewById(R.id.attribute_list_view);
        listView.setAdapter(attributeListAdapter);
        return view;
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent(View rootView) {
        Button yes = (Button) rootView.findViewById(R.id.yes);
        Button no = (Button) rootView.findViewById(R.id.no);
        final AttributeDialog selfThis = this;
        //设置确定按钮被点击后，向外界提供监听
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (yesOnclickListener != null) {
                    //更新属性值
                    Map<String,Object> attributes = attributeListAdapter.updateAttributes();
                    if (yesOnclickListener.onYesClick(attributes)) {
                        selfThis.dismiss();
                    }
                }
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noOnclickListener != null) {
                    noOnclickListener.onNoClick();
                }
                selfThis.dismiss();
            }
        });
    }

    /**
     * 设置确定按钮和取消被点击的接口
     */
    public interface onYesOnclickListener {
        boolean onYesClick(Map<String,Object> attributes);
    }

    public interface onNoOnclickListener {
        void onNoClick();
    }
}
