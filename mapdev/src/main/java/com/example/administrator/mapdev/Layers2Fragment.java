package com.example.administrator.mapdev;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Layers2Fragment extends Fragment {
    LayersManager layersManager;
    ExpandableListView listView;

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
        listView = (ExpandableListView) view.findViewById(R.id.layers2_listView);
        LayersAdapter layersAdapter = new LayersAdapter(getContext(), layersManager);
        listView.setAdapter(layersAdapter);
        for (int i = 0; i < layersAdapter.getGroupCount(); i++) {
            listView.expandGroup(i);
        }
        return view;
    }

    public void onButtonPressed() {
        getFragmentManager().popBackStack();
    }

    //对图层进行分组
    class GroupLayerItem implements Comparator {
        public String groupName;
        public List<LayerItemData> items = new ArrayList<>();
        public int groupLayerType;

        @Override
        public int compare(Object lhs, Object rhs) {
            GroupLayerItem item1 = (GroupLayerItem) lhs;
            GroupLayerItem item2 = (GroupLayerItem) rhs;
            if (item1.groupLayerType < item2.groupLayerType)
                return 1;
            return 0;
        }
    }

    public List<LayerItemData> sortLayerItems(List<LayerItemData> items) {
        Collections.sort(items, new Comparator<LayerItemData>() {
            @Override
            public int compare(LayerItemData lhs, LayerItemData rhs) {
                //假如A的值大于B，你返回1。这样调用Collections.sort()方法就是升序
                //假如A的值大于B，你返回-1。这样调用Collections.sort()方法就是降序
                if (lhs.getLayerType() < rhs.getLayerType()) {  //asc
                    return -1;
                } else if (lhs.getLayerType() == rhs.getLayerType()) {
                    if (lhs.getOrderId() < rhs.getOrderId()) //orderId desc
                        return 1;
                    else if (lhs.getOrderId() == rhs.getOrderId())
                        return 0;
                    else
                        return -1;
                } else
                    return 1;
            }
        });
        return items;
    }

    class LayersAdapter extends BaseExpandableListAdapter {
        private LayersManager layersManager;
        private List<GroupLayerItem> groupItems; //适配器的数据源
        private LayoutInflater m_Inflater;

        public LayersAdapter(Context context, LayersManager layersManager) {
            m_Inflater = LayoutInflater.from(context);
            this.layersManager = layersManager;
            //初始化分组
            groupItems = new ArrayList<>();
            List<LayerItemData> items = this.layersManager.getLayerItems();
            sortLayerItems(items);
            int layerType = -1;
            GroupLayerItem groupLayerItem = new GroupLayerItem();
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
            for (LayerItemData item : items) {
                if (item.getLayerType() != layerType) {
                    layerType = item.getLayerType();
                    groupLayerItem = new GroupLayerItem();
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
            TextView textView = (TextView) convertView.findViewById(R.id.layer_group_name);
            textView.setText(groupItem.groupName);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            convertView = m_Inflater.inflate(R.layout.item_layerlist2, null);
            final LayerItemData item = groupItems.get(groupPosition).items.get(childPosition);
            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox2);
            TextView textView = (TextView) convertView.findViewById(R.id.item_layer2_name);
            checkBox.setChecked(item.getVisible());
            String dataSource = item.getDataSource();
            String fileName = dataSource.substring(dataSource.lastIndexOf("/") + 1);
            textView.setText(fileName);

            //Event
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    item.getLayer().setVisible(isChecked);
                    item.setVisible(isChecked);
                    if(item.getId() != 0 )
                        item.update(item.getId());
                }
            });
            ImageButton button = (ImageButton) convertView.findViewById(R.id.item_layer2_set);
            final View itemView = convertView;
            final int groupPos = groupPosition;
            final int childPos = childPosition;
            final ImageButton delButton = (ImageButton) convertView.findViewById(R.id.item_layer2_delete);
            delButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Layers2Fragment.this.getContext(),
                            R.style.AppCompatAlertDialogStyle);
                    builder.setTitle("图层管理");
                    builder.setMessage("是否删除图层?");
                    builder.setCancelable(false);
                    builder.setNegativeButton("取消", null);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (layersManager.deleteLayer(item)) {
//                                listView.removeView(itemView);
                                groupItems.get(groupPos).items.remove(childPos);
                                LayersAdapter.this.notifyDataSetChanged();
                            }
                        }
                    });
                    builder.show();
                }
            });
            ImageButton upButton = (ImageButton) convertView.findViewById(R.id.item_layers2_up);
            ImageButton downButton = (ImageButton) convertView.findViewById(R.id.item_layers2_down);
            upButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GroupLayerItem groupItem = groupItems.get(groupPos);
                    if (childPos == 0) {
                        MapApplication.showMessage("不能移动图层到其他图层类型组中");
                        return;
                    }
                    LayerItemData one = groupItem.items.get(childPos);
                    LayerItemData two = groupItem.items.get(childPos - 1);
                    layersManager.exchangeTwoLayerItems(one, two);
                    Collections.swap(groupItem.items, childPos, childPos - 1);
                    LayersAdapter.this.notifyDataSetChanged();
                }
            });
            downButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GroupLayerItem groupItem = groupItems.get(groupPos);
                    if ( (childPos+1) == groupItem.items.size() ) {
                        MapApplication.showMessage("不能移动图层到其他图层类型组中");
                        return;
                    }
                    LayerItemData one = groupItem.items.get(childPos);
                    LayerItemData two = groupItem.items.get(childPos + 1);
                    layersManager.exchangeTwoLayerItems(one, two);
                    Collections.swap(groupItem.items, childPos, childPos + 1);
                    LayersAdapter.this.notifyDataSetChanged();
                }
            });
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
