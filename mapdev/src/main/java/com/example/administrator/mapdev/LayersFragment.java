package com.example.administrator.mapdev;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

import java.util.ArrayList;

/**
 * 图层面板
 * Activities that contain this fragment must implement the
 * {@link LayersFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LayersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LayersFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	private OnFragmentInteractionListener mListener;

	private DragSortListView listView;
	private LayersAdapter adapter;
	private LayersManager layersManager;
	private ArrayList<LayerItemData> listItemData;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment LayersFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static LayersFragment newInstance(LayersManager layersManager, String param1, String param2) {
		LayersFragment fragment = new LayersFragment();
		fragment.layersManager = layersManager;
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);

		fragment.setArguments(args);
		return fragment;
	}

	public LayersFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_layers, container, false);

		//listData
		listView = (DragSortListView) view.findViewById(R.id.layer_list);

		//得到滑动listView并且设置监听器。
		listView.setDropListener(onDrop);
		listView.setRemoveListener(onRemove);
		adapter = new LayersAdapter(inflater.getContext(), layersManager);
		listView.setAdapter(adapter);
		listView.setDragEnabled(true); //设置是否可拖动。

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

//		// Refresh the state of the +1 button each time the activity receives focus.
//		mPlusOneButton.initialize(PLUS_ONE_URL, PLUS_ONE_REQUEST_CODE);
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			mListener = (OnFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		void onFragmentInteraction(Uri uri);
	}

	//监听器在手机拖动停下的时候触发
	private DragSortListView.DropListener onDrop =
			new DragSortListView.DropListener() {
				@Override
				public void drop(int from, int to) {//from to 分别表示 被拖动控件原位置 和目标位置
					if (from != to) {
						LayerItemData item = (LayerItemData) adapter.getItem(from);//得到listview的适配器
						adapter.remove(from);//在适配器中”原位置“的数据。
						adapter.insert(item, to);//在目标位置中插入被拖动的控件。
					}
				}
			};

	//删除监听器，点击左边差号就触发。删除item操作。
	private RemoveListener onRemove =
			new DragSortListView.RemoveListener() {
				@Override
				public void remove(int which) {
					adapter.remove(which);
				}
			};
}
