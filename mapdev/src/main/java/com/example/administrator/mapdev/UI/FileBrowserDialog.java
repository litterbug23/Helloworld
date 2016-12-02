package com.example.administrator.mapdev.UI;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.administrator.mapdev.IFileSelectCallback;
import com.example.administrator.mapdev.MapApplication;
import com.example.administrator.mapdev.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/11/12.
 */
public class FileBrowserDialog extends Dialog {
    private static final String ARG_SUFFIX = "suffix";
    private static final String ARG_INITIAL_DIR = "initialDirectory";
    //操作类型
    public static final int ACTION_UP = 0;
    public static final int ACTION_FILE = 1;
    public static final int ACTION_DIR = 2;
    private String mSuffix;

    private String mInitialDir;
    private FileSelectView mFileSelectView = null;
    private int mActionType = 0;
    private String mSelectedPath;

    public FileBrowserDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        mSuffix = ".tiff;.tif;.img;";
        mInitialDir = MapApplication.instance().getDataPath();
    }

    public FileBrowserDialog(@NonNull Context context) {
        super(context);
        mSuffix = ".tiff;.tif;.img;";
        mInitialDir = MapApplication.instance().getDataPath();
    }

    public String getSuffix() {
        return mSuffix;
    }

    public void setSuffix(String mSuffix) {
        this.mSuffix = mSuffix;
    }

    public String getInitialDir() {
        return mInitialDir;
    }

    public void setInitialDir(String mInitialDir) {
        this.mInitialDir = mInitialDir;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = createView(inflater, null, savedInstanceState);
        super.setContentView(contentView);
    }

    private View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dialog_file_browser, container, false);
        mFileSelectView = (FileSelectView) view.findViewById(R.id.fileListView);
        Map<String, Integer> images = new HashMap<>();
        // 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
        images.put(FileSelectView.sRoot, R.drawable.filedialog_root);    // 根目录图标
        images.put(FileSelectView.sParent, R.drawable.filedialog_folder_up);    //返回上一层的图标
        images.put(FileSelectView.sFolder, R.drawable.filedialog_folder);    //文件夹图标
        images.put("tiff", R.drawable.image);
        images.put("tif", R.drawable.image);
        images.put("img", R.drawable.image);
        images.put("shp", R.drawable.vector);
        images.put("kml", R.drawable.vector);
        images.put(FileSelectView.sEmpty, R.drawable.filedialog_root);
        mFileSelectView.setImageMap(images);
        mFileSelectView.setSuffix(mSuffix);
        mFileSelectView.setPath(mInitialDir);
        mFileSelectView.refreshFileList();
        mFileSelectView.setFileCallbackBundle(new IFileSelectCallback() {
            @Override
            public void onSelectDirectory(String path) {
                //Bundle arguments = new Bundle();
                //arguments.putString("path", path);
                //onButtonPressed(getId(), ACTION_DIR, mDataType, arguments);
                mActionType = ACTION_DIR;
                mSelectedPath = path;
            }

            @Override
            public void onSelectFile(String path, String filename) {
                //toolbar.setTitle(path + filename);
                //Bundle arguments = new Bundle();
                //arguments.putString("path", path);
                //arguments.putString("file", filename);
                //onButtonPressed(getId(), ACTION_FILE, mDataType, arguments);
                mActionType = ACTION_FILE;
                mSelectedPath = path;
                setTitle(mSelectedPath);
            }
        });
        initEvent(view);
        return view;
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent(View rootView) {
        Button yes = (Button) rootView.findViewById(R.id.yes);
        Button no = (Button) rootView.findViewById(R.id.no);
        final FileBrowserDialog selfThis = this;
        //设置确定按钮被点击后，向外界提供监听
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedPath == null) {
                    MapApplication.showMessage("需要选中一个文件");
                    return;
                }
                if (yesOnclickListener != null) {
                    if (yesOnclickListener.onYesClick(mActionType, mSelectedPath)) {
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
                    noOnclickListener.onNoClick(mActionType, mSelectedPath);
                }
                selfThis.dismiss();
            }
        });
    }

    /**
     * 设置确定按钮和取消被点击的接口
     */
    public interface onYesOnclickListener {
        boolean onYesClick(int action_type, String path);
    }

    public interface onNoOnclickListener {
        void onNoClick(int action_type, String path);
    }

}
