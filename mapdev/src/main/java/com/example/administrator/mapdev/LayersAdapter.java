package com.example.administrator.mapdev;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


/**
 * Created by Administrator on 2016/3/11.
 */
public class LayersAdapter extends BaseAdapter {
	private Context context;
	List<LayerItemData> items;//适配器的数据源
	LayersManager layersManager;
	final static int geometryTypeLogo[] = {R.drawable.unknow, R.drawable.point,
			R.drawable.polyline, R.drawable.polygon, R.drawable.raster, R.drawable.text};

	public LayersAdapter(Context context, LayersManager layersManager) {
		this.context = context;
		this.layersManager = layersManager;
		this.items = layersManager.getLayerItems();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return items.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return items.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	public void remove(int arg0) {//删除指定位置的item
		//items.remove(arg0);
		layersManager.removeLayer(arg0);
		this.notifyDataSetChanged();//不要忘记更改适配器对象的数据源
	}

	public void insert(LayerItemData item, int arg0) {//在指定位置插入item
		//items.add(arg0, item);
		layersManager.insertLayer(item, arg0);
		this.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayerItemData item = (LayerItemData) getItem(position);
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder(item);
			convertView = LayoutInflater.from(context).inflate(R.layout.item_layerlist, null);
			viewHolder.setCheckBox((CheckBox) convertView.findViewById(R.id.checkBox));
			viewHolder.setDataSource( (TextView) convertView.findViewById(R.id.data_source));
			viewHolder.setLayerType( (TextView) convertView.findViewById(R.id.layer_type));
			viewHolder.setGeometryType( (ImageView) convertView.findViewById(R.id.geometry_type));
			viewHolder.setIvSetting( (ImageButton) convertView.findViewById(R.id.drag_setting));
			viewHolder.setIvDragHandle( (ImageView) convertView.findViewById(R.id.drag_handle));
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
			viewHolder.update(item);
		}

		return convertView;
	}

	class ViewHolder {
		private CheckBox checkBox;
		private TextView dataSource;
		private TextView layerType;
		private ImageView geometryType;
		private ImageButton ivSetting;
		private ImageView ivDragHandle;
		private LayerItemData layerItem;

		public ViewHolder(LayerItemData item){
			layerItem=item;
		}

		public CheckBox getCheckBox() {
			return checkBox;
		}

		public ViewHolder setCheckBox(CheckBox checkBox) {
			this.checkBox = checkBox;
			this.checkBox.setOnCheckedChangeListener(null);
			if( layerItem.getVisible())
				this.checkBox.setChecked(true);
			else
				this.checkBox.setChecked(false);
			attachLayerVisibleEvent();
			return this;
		}

		public TextView getDataSource() {
			return dataSource;
		}

		public ViewHolder setDataSource(TextView dataSource) {
			this.dataSource = dataSource;
			dataSource.setText(layerItem.getDataSource());
			return this;
		}

		public TextView getLayerType() {
			return layerType;
		}

		public ViewHolder setLayerType(TextView layerType) {
			this.layerType = layerType;
			layerType.setText(layerItem.getLayerTypeString());
			return this;
		}

		public ImageView getGeometryType() {
			return geometryType;
		}

		public ViewHolder setGeometryType(ImageView geometryType) {
			this.geometryType = geometryType;
			geometryType.setImageResource(geometryTypeLogo[layerItem.getGeometryType()]);
			return this;
		}

		public ImageButton getIvSetting() {
			return ivSetting;
		}

		public ViewHolder setIvSetting(ImageButton ivSetting) {
			this.ivSetting = ivSetting;
			attachButtonSetting();
			return this;
		}

		public ImageView getIvDragHandle() {
			return ivDragHandle;
		}

		public ViewHolder setIvDragHandle(ImageView ivDragHandle) {
			this.ivDragHandle = ivDragHandle;
			return this;
		}

		public LayerItemData getLayerItem() {
			return layerItem;
		}

		public void update(LayerItemData layerItem){
			this.layerItem=layerItem;
			checkBox.setChecked(layerItem.getVisible());
			dataSource.setText(layerItem.getDataSource());
			layerType.setText(layerItem.getLayerTypeString());
			geometryType.setImageResource(geometryTypeLogo[layerItem.getGeometryType()]);
			attachButtonSetting();
			attachLayerVisibleEvent();
		}

		private void attachLayerVisibleEvent() {
			checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					layerItem.setVisible(isChecked);
					if (layerItem.getLayer() != null)
						layerItem.getLayer().setVisible(isChecked);
					layerItem.save();
				}
			});
		}

		private void attachButtonSetting() {
//			ivSetting.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					//  通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
//					AlertDialog.Builder builder = new AlertDialog.Builder(MapApplication.getContext(),R.style.AppTheme);
//					//    设置Title的图标
//					builder.setIcon(R.mipmap.ic_launcher);
//					//    设置Title的内容
//					builder.setTitle("提示信息");
//					//    设置Content来显示一个信息
//					builder.setMessage("确定删除吗？");
//					//    设置一个PositiveButton
//					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							Toast.makeText(MapApplication.getContext(), "positive: " + which, Toast.LENGTH_SHORT).show();
//
//						}
//					});
//					//    设置一个NegativeButton
//					builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							Toast.makeText(MapApplication.getContext(), "negative: " + which, Toast.LENGTH_SHORT).show();
//						}
//					});
//					//  显示出该对话框
//					builder.show();
//				}
//			});
		}
	}
}
