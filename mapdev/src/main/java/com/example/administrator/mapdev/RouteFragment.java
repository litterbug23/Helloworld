package com.example.administrator.mapdev;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;

/**
 * Created by Administrator on 2016/4/18.
 */
public class RouteFragment extends Fragment {
	private SwipeMenuListView mSwipeListView;
	private DataAdapter mAdapter;
	private GpsRouteTracker mGpsRouteTracker = null;

	public RouteFragment() {

	}

	public static RouteFragment newInstance(GpsRouteTracker routeTracker) {
		RouteFragment fragment = new RouteFragment();
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
		View view = inflater.inflate(R.layout.fragment_route, container, false);
		final Toolbar toolbar = (Toolbar) view.findViewById(R.id.route_toolbar);
		toolbar.setNavigationIcon(R.drawable.ic_action_back);
		mSwipeListView = (SwipeMenuListView) view.findViewById(R.id.routeListView);
		mAdapter = new DataAdapter();
		mSwipeListView.setAdapter(mAdapter);
		// step 1. create a MenuCreator
		SwipeMenuCreator creator = new SwipeMenuCreator() {
			@Override
			public void create(SwipeMenu menu, int position) {
				// create "open" item
				SwipeMenuItem openItem = new SwipeMenuItem(
						MapApplication.getContext());
				String routeName = mGpsRouteTracker.getRouteNameList().get(position);
				boolean isLoaded = mGpsRouteTracker.isShowRouteTracker(routeName);
				// set item background
				openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
						0xCE)));
				// set item width
				openItem.setWidth(dp2px(90));
				// set item title
				if (!isLoaded)
					openItem.setTitle("打开");
				else
					openItem.setTitle("关闭");
				// set item title fontsize
				openItem.setTitleSize(18);
				// set item title font color
				openItem.setTitleColor(Color.WHITE);
				// add to menu
				menu.addMenuItem(openItem);
				// create "delete" item
				SwipeMenuItem deleteItem = new SwipeMenuItem(
						MapApplication.getContext());
				// set item background
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
						0x3F, 0x25)));
				// set item width
				deleteItem.setWidth(dp2px(90));
				// set a icon
				deleteItem.setIcon(R.drawable.ic_delete);
				// add to menu
				menu.addMenuItem(deleteItem);
			}
		};
		// set creator
		mSwipeListView.setMenuCreator(creator);

		// step 2. listener item click event
		mSwipeListView.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public void onMenuItemClick(int position, SwipeMenu menu, int index) {
				String name = mGpsRouteTracker.getRouteNameList().get(position);
				switch (index) {
					case 0:
						// open
						SwipeMenuItem menuItem = menu.getMenuItem(index);
						if (menuItem.getTitle().equals("打开")) {
							menuItem.setTitle("关闭");
							mSwipeListView.updateSwipeMenu(position, 0);
							open(name);
						} else {
							menuItem.setTitle("打开");
							mSwipeListView.updateSwipeMenu(position, 0);
							close(name);
						}
						break;
					case 1:
						// delete
						delete(name);
						mAdapter.notifyDataSetChanged();
						break;
				}
			}
		});

		// set SwipeListener
		mSwipeListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

			@Override
			public void onSwipeStart(int position) {
				// swipe start
			}

			@Override
			public void onSwipeEnd(int position) {
				// swipe end
			}
		});
		return view;
	}

	private void close(String item) {
		mGpsRouteTracker.closeRouteTracker(item);
	}

	private void delete(String item) {
		// delete
		mGpsRouteTracker.removeRouteTracker(item);
	}

	private void open(String item) {
		// open
		mGpsRouteTracker.showRouteTracker(item);
	}

	/**
	 *
	 */
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
						R.layout.item_list_route, null);
				new ViewHolder(convertView,position);
			}
			ViewHolder holder = (ViewHolder) convertView.getTag();
			String item = (String) getItem(position);
			holder.tv_name.setText(item);
			return convertView;
		}

		class ViewHolder {
			ImageView iv_icon;
			TextView tv_name;

			public ViewHolder(View view,int position) {
//				String routeName = mGpsRouteTracker.getRouteNameList().get(position);
//				boolean isLoaded = mGpsRouteTracker.isShowRouteTracker(routeName);
				iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
//				if(isLoaded)
//					iv_icon.setImageResource(R.drawable.route_line);
//				else
//					iv_icon.setImageResource(R.drawable.ic_route_line);
				tv_name = (TextView) view.findViewById(R.id.tv_name);
				view.setTag(this);
			}
		}
	}

	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}
}
