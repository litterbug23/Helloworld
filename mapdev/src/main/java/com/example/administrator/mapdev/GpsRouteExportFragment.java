package com.example.administrator.mapdev;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/4/19.
 */
public class GpsRouteExportFragment extends Fragment {
	private GpsRouteTracker mGpsRouteTracker = null;
	private ListView mListView = null;

	public GpsRouteExportFragment() {
	}

	public static GpsRouteExportFragment newInstance(GpsRouteTracker routeTracker) {
		GpsRouteExportFragment fragment = new GpsRouteExportFragment();
		fragment.mGpsRouteTracker = routeTracker;
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_route_export, container, false);
		final Toolbar toolbar = (Toolbar) view.findViewById(R.id.route_toolbar2);
		toolbar.setNavigationIcon(R.drawable.ic_action_back);
		mListView = (ListView) view.findViewById(R.id.routeListView2);
		DataAdapter adapter = new DataAdapter();
		mListView.setAdapter(adapter);
		return view;
	}

	private class DataAdapter extends BaseAdapter {
		DataAdapter() {
		}

		@Override
		public int getCount() {
			return mGpsRouteTracker.getRouteNameList().size();
		}

		@Override
		public Object getItem(int position) {
			return mGpsRouteTracker.getRouteNameList().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(MapApplication.getContext(),
						R.layout.item_list_route_export, null);
				new ViewHolder(convertView, position);
			}
			ViewHolder holder = (ViewHolder) convertView.getTag();
			String item = (String) getItem(position);
			holder.tv_name.setText(item);
			return convertView;
		}

		class ViewHolder {
			CheckBox checkBox;
			ImageView iv_icon;
			TextView tv_name;

			public ViewHolder(View view, int position) {
				checkBox=(CheckBox)view.findViewById(R.id.checkBox2);
				iv_icon = (ImageView) view.findViewById(R.id.iv_icon2);
				tv_name = (TextView) view.findViewById(R.id.tv_name2);
				view.setTag(this);
			}
		}
	}

}
