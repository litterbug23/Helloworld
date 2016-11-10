package com.example.administrator.mapdev;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Layers2Fragment extends Fragment {
    LayersManager layersManager;

    public Layers2Fragment() {
        // Required empty public constructor
    }

    public static Layers2Fragment newInstance(LayersManager layersManager) {
        Layers2Fragment fragment = new Layers2Fragment();
        fragment.layersManager = layersManager;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_layers2, container, false);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.scene_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed();
            }
        });
        ExpandableListView listView = (ExpandableListView)view.findViewById(R.id.layers2_listView);
        LayersAdapter layersAdapter = new LayersAdapter(getContext(),layersManager);
        listView.setAdapter(layersAdapter);
        for(int i = 0; i < layersAdapter.getGroupCount(); i++){
            listView.expandGroup(i);
        }
        return view;
    }

    public void onButtonPressed() {
        getFragmentManager().popBackStack();
    }

    //对图层进行分组
    class GroupLayerItem {
        public String groupName;
        public List<LayerItemData> items = new ArrayList<>();
    }

    class LayersAdapter extends BaseExpandableListAdapter  {
        private LayersManager layersManager;
        private List<GroupLayerItem> groupItems; //适配器的数据源
        private LayoutInflater m_Inflater;

        public LayersAdapter(Context context, LayersManager layersManager) {
            m_Inflater =  LayoutInflater.from(context);
            this.layersManager = layersManager;
            //初始化分组
            groupItems=new ArrayList<>();
            List<LayerItemData> items = this.layersManager.getLayerItems();
            int layerType= -1;
            GroupLayerItem groupLayerItem=new GroupLayerItem();
            //TODO:
//            groupLayerItem=new GroupLayerItem();
//            groupLayerItem.groupName= LayerItemData.layerTypeStrings[LayerItemData.GRAPHIC_LAYER];
//            LayerItemData photoItem =  new LayerItemData();
//            photoItem.setLayerType(LayerItemData.GRAPHIC_LAYER);
//            photoItem.setGeometryType(LayerItemData.POINT);
//            photoItem.setDataSource("采集照片数据");
//            groupLayerItem.items.add(photoItem);
//            LayerItemData pointItem =  new LayerItemData();
//            pointItem.setLayerType(LayerItemData.GRAPHIC_LAYER);
//            pointItem.setGeometryType(LayerItemData.POINT);
//            pointItem.setDataSource("采集点数据");
//            groupLayerItem.items.add(pointItem);
//            LayerItemData polylineItem =  new LayerItemData();
//            polylineItem.setLayerType(LayerItemData.GRAPHIC_LAYER);
//            polylineItem.setGeometryType(LayerItemData.POLYGON);
//            polylineItem.setDataSource("采集线数据");
//            groupLayerItem.items.add(polylineItem);
//            LayerItemData polygonItem =  new LayerItemData();
//            polygonItem.setLayerType(LayerItemData.GRAPHIC_LAYER);
//            polygonItem.setGeometryType(LayerItemData.POLYGON);
//            polygonItem.setDataSource("采集面数据");
//            groupLayerItem.items.add(polygonItem);
//            groupItems.add(groupLayerItem);
            for( LayerItemData item : items ){
                if( item.getLayerType() != layerType ){
                    layerType = item.getLayerType();
                    groupLayerItem=new GroupLayerItem();
                    groupLayerItem.groupName = item.getLayerTypeString();
                    groupItems.add(groupLayerItem);
                }
                groupLayerItem.items.add(item);
            }
        }

        @Override
        public int getGroupCount() {
            return groupItems.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return groupItems.get(groupPosition).items.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupItems.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return groupItems.get(groupPosition).items.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            convertView = m_Inflater.inflate(R.layout.item_layers_group, null);
            GroupLayerItem groupItem = groupItems.get(groupPosition);
            TextView textView = (TextView)convertView.findViewById(R.id.layer_group_name);
            textView.setText(groupItem.groupName);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            convertView = m_Inflater.inflate(R.layout.item_layerlist2, null);
            LayerItemData item = groupItems.get(groupPosition).items.get(childPosition);
            CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.checkBox2);
            TextView textView = (TextView)convertView.findViewById(R.id.item_layer2_name);
            checkBox.setChecked( item.getVisible() );
            textView.setText(item.getDataSource());
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
