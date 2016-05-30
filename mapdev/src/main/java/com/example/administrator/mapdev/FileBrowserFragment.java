package com.example.administrator.mapdev;

import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FileBrowserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FileBrowserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileBrowserFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_SUFFIX = "suffix";
	private static final String ARG_INITIAL_DIR = "initialDirectory";
	private static final String ARG_DATA_TYPE = "dataType";

	// TODO: Rename and change types of parameters
	private String mSuffix;
	private String mInitialDir;
	private int mDataType;

	private FileSelectView mFileSelectView = null;
	private OnFragmentInteractionListener mListener;

	//操作类型
	public static final int ACTION_UP = 0;
	public static final int ACTION_FILE = 1;
	public static final int ACTION_DIR = 2;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param suffix           Parameter 1.
	 * @param initialDirectory Parameter 2.
	 * @return A new instance of fragment FileBrowserFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static FileBrowserFragment newInstance(int dataType, String suffix, String initialDirectory) {
		FileBrowserFragment fragment = new FileBrowserFragment();
		Bundle args = new Bundle();
		args.putString(ARG_SUFFIX, suffix);
		args.putString(ARG_INITIAL_DIR, initialDirectory);
		args.putInt(ARG_DATA_TYPE, dataType);
		fragment.setArguments(args);
		return fragment;
	}

	public FileBrowserFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mDataType = getArguments().getInt(ARG_DATA_TYPE);
			mSuffix = getArguments().getString(ARG_SUFFIX);
			mInitialDir = getArguments().getString(ARG_INITIAL_DIR);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_file_browser, container, false);

		final Toolbar toolbar = (Toolbar) view.findViewById(R.id.filetoolbar);
		toolbar.setNavigationIcon(R.drawable.ic_action_back);
		toolbar.setTitle(mInitialDir);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle args = new Bundle();
				args.putString("path", mFileSelectView.getPath());
				onButtonPressed(getId(), ACTION_UP,mDataType, args);
			}
		});

		mFileSelectView = (FileSelectView) view.findViewById(R.id.fileListView);
		Map<String, Integer> images = new HashMap<String, Integer>();
		// 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
		images.put(FileSelectView.sRoot, R.drawable.filedialog_root);    // 根目录图标
		images.put(FileSelectView.sParent, R.drawable.filedialog_folder_up);    //返回上一层的图标
		images.put(FileSelectView.sFolder, R.drawable.filedialog_folder);    //文件夹图标
		images.put("tiff", R.drawable.image);
		images.put("tif", R.drawable.image);
		images.put("img", R.drawable.image);
		images.put("shp", R.drawable.vector);
		images.put("kml",R.drawable.vector);
		images.put("gdb",R.drawable.vector);
		images.put(FileSelectView.sEmpty, R.drawable.filedialog_root);
		mFileSelectView.setImageMap(images);
		mFileSelectView.setSuffix(mSuffix);
		mFileSelectView.setPath(mInitialDir);
		mFileSelectView.refreshFileList();

		mFileSelectView.setFileCallbackBundle(new IFileSelectCallback() {
			@Override
			public void onSelectDirectory(String path) {
				toolbar.setTitle(path);
				Bundle arguments = new Bundle();
				arguments.putString("path", path);
				onButtonPressed(getId(), ACTION_DIR, mDataType, arguments);
			}

			@Override
			public void onSelectFile(String path, String filename) {
				toolbar.setTitle(path + filename);
				Bundle argumens = new Bundle();
				argumens.putString("path", path);
				argumens.putString("file", filename);
				onButtonPressed(getId(), ACTION_FILE, mDataType, argumens);
			}
		});
		return view;
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(int fragment_id, int action_type, int data_type, @Nullable Bundle args) {
		if (mListener != null) {
			mListener.onFragmentInteraction(fragment_id, action_type, data_type, args);
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
		void onFragmentInteraction(int fragment_id, int action_type, int data_type, @Nullable Bundle args);
	}
}
