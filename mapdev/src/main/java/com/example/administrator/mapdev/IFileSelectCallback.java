package com.example.administrator.mapdev;
import android.os.Bundle;

/**
 * Created by Administrator on 2016/3/14.
 */
public interface IFileSelectCallback {
	abstract void onSelectDirectory(String path);
	abstract void onSelectFile(String path,String filename);
}
