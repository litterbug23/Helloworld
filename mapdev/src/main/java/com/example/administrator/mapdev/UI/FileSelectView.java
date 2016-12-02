package com.example.administrator.mapdev.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.administrator.mapdev.IFileSelectCallback;
import com.example.administrator.mapdev.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/14.
 */
// 参数说明
// context:上下文
// dialogid:对话框ID
// title:对话框标题
// callback:一个传递Bundle参数的回调接口
// suffix:需要选择的文件后缀，比如需要选择wav、mp3文件的时候设置为".wav;.mp3;"，注意最后需要一个分号(;)
// images:用来根据后缀显示的图标资源ID。
//	根目录图标的索引为sRoot;
//	父目录的索引为sParent;
//	文件夹的索引为sFolder;
//	默认图标的索引为sEmpty;
//	其他的直接根据后缀进行索引，比如.wav文件图标的索引为"wav"
public class FileSelectView extends ListView implements AdapterView.OnItemClickListener {

	public static String tag = "OpenFileDialog";
	static final public String sRoot = "/";
	static final public String sParent = "..";
	static final public String sFolder = ".";
	static final public String sEmpty = "";
	static final private String sOnErrorMsg = "No rights to access!";

	private IFileSelectCallback callback = null;
	private String path = sRoot;
	private List<Map<String, Object>> list = null;
	private int dialogid = 0;
	private String suffix = null;
	private Map<String, Integer> imageMap = null;

	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	public Map<String, Integer> getImageMap() {
		return imageMap;
	}

	public void setImageMap(Map<String, Integer> imagemap) {
		if(imagemap!=null) {
			if(this.imageMap != null ) {
				Iterator<Map.Entry<String, Integer>> entries = imagemap.entrySet().iterator();
				while (entries.hasNext()) {
					Map.Entry<String, Integer> entry = entries.next();
					this.imageMap.put(entry.getKey(), entry.getValue());
				}
			}
			else {
				this.imageMap =imagemap;
			}
		}
	}

	public FileSelectView(Context context,AttributeSet attrs){
		super(context, attrs);
		this.setOnItemClickListener(this);
	}

	public FileSelectView(Context context, int dialogid, IFileSelectCallback callback, String suffix, Map<String, Integer> images) {
		super(context);
		setImageMap(images);
		this.suffix = suffix==null?"":suffix.toLowerCase();
		this.callback = callback;
		this.dialogid = dialogid;
		this.setOnItemClickListener(this);
		refreshFileList();
	}

	private String getSuffix(String filename){
		int dix = filename.lastIndexOf('.');
		if(dix<0){
			return "";
		}
		else{
			return filename.substring(dix+1);
		}
	}

	private int getImageId(String s){
		if(imageMap == null){
			return 0;
		}
		else if(imageMap.containsKey(s)){
			return imageMap.get(s);
		}
		else if(imageMap.containsKey(sEmpty)){
			return imageMap.get(sEmpty);
		}
		else {
			return 0;
		}
	}

	public int refreshFileList()
	{
		// 刷新文件列表
		File[] files = null;
		try{
			files = new File(path).listFiles();
		}
		catch(Exception e){
			files = null;
		}
		if(files==null){
			// 访问出错
			Toast.makeText(getContext(), sOnErrorMsg, Toast.LENGTH_SHORT).show();
			return -1;
		}
		if(list != null){
			list.clear();
		}
		else{
			list = new ArrayList<Map<String, Object>>(files.length);
		}

		// 用来先保存文件夹和文件夹的两个列表
		ArrayList<Map<String, Object>> lfolders = new ArrayList<Map<String, Object>>();
		ArrayList<Map<String, Object>> lfiles = new ArrayList<Map<String, Object>>();

		if(!this.path.equals(sRoot)){
			// 添加根目录 和 上一层目录
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("name", sRoot);
			map.put("path", sRoot);
			map.put("img", getImageId(sRoot));
			list.add(map);

			map = new HashMap<String, Object>();
			map.put("name", sParent);
			map.put("path", path);
			map.put("img", getImageId(sParent));
			list.add(map);
		}

		for(File file: files)
		{
			if(file.isDirectory() && file.listFiles()!=null){
				// 添加文件夹
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("name", file.getName());
				map.put("path", file.getPath());
				map.put("img", getImageId(sFolder));
				lfolders.add(map);
			}
			else if(file.isFile()){
				// 添加文件
				String sf = getSuffix(file.getName()).toLowerCase();
				if(suffix == null || suffix.length()==0 || (sf.length()>0 && suffix.indexOf("."+sf+";")>=0)){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("name", file.getName());
					map.put("path", file.getPath());
					map.put("img", getImageId(sf));
					lfiles.add(map);
				}
			}
		}

		list.addAll(lfolders); // 先添加文件夹，确保文件夹显示在上面
		list.addAll(lfiles);	//再添加文件


		SimpleAdapter adapter = new SimpleAdapter(getContext(), list, R.layout.filedialogitem, new String[]{"img", "name", "path"}, new int[]{R.id.filedialogitem_img, R.id.filedialogitem_name, R.id.filedialogitem_path});
		this.setAdapter(adapter);
		return files.length;
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		// 条目选择
		String pt = (String) list.get(position).get("path");
		String fn = (String) list.get(position).get("name");
		if(fn.equals(sRoot) || fn.equals(sParent)){
			// 如果是更目录或者上一层
			File fl = new File(pt);
			String ppt = fl.getParent();
			if(ppt != null){
				// 返回上一层
				path = ppt;
			}
			else{
				// 返回更目录
				path = sRoot;
			}
		}
		else{
			File fl = new File(pt);
			if(fl.isFile()){
				//将关闭对话框的操作，由调用对象来操作
				// 如果是文件
//				((Activity)getContext()).dismissDialog(this.dialogid); // 让文件夹对话框消失
				// 设置回调的返回值
				// 调用事先设置的回调函数
				if(this.callback!=null)
					this.callback.onSelectFile(pt,fn);
				return;
			}
			else if(fl.isDirectory()){
				// 如果是文件夹
				// 那么进入选中的文件夹
				if(this.callback!=null)
					this.callback.onSelectDirectory(pt);
				path = pt;
			}
		}
		this.refreshFileList();
	}

	public void setFileCallbackBundle(IFileSelectCallback callback){
		this.callback=callback;
	}
}
