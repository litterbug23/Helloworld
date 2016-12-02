package com.example.administrator.mapdev;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Environment;
import android.widget.Toast;

import org.gdal.gdal.gdal;
import org.gdal.ogr.ogr;
import org.litepal.LitePalApplication;

import java.io.File;

/**
 * Created by Administrator on 2016/3/26.
 */
public class MapApplication extends LitePalApplication {
	private GpsLocationService gpsLocationService;
	private SensorService sensorService;
	private String basePath = "/外业核查";
	private String gpsPath="/外业核查/导出数据/GPS轨迹";
	private String dataPath="/外业核查/原始数据";
	private String outputPath ="/外业核查/导出数据";
	private String photoPath = "/外业核查/导出数据/照片";
	private String smallPhotoPath="/外业核查/导出数据/照片缩略图";
	private LayersManager layersManager;
	private Activity mainActivity;

	static public MapApplication instance(){
		return (MapApplication)getContext();
	}

	static public void showMessage(String message) {
		Toast.makeText(MapApplication.getContext(), message, Toast.LENGTH_LONG).show();
		//Snackbar.make(MapApplication.getContext(), message , Snackbar.LENGTH_LONG).show();
	}

//	static public void showAlertMessage(String title,String message) {
//		AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);
//		builder.setTitle(title);
//		builder.setMessage(message);
//		builder.setPositiveButton("确定", null);
//		builder.show();
//	}

	public LayersManager getLayersManager() {
		return layersManager;
	}

	public void setLayersManager(LayersManager layersManager) {
		this.layersManager = layersManager;
	}

	public Activity getMainActivity() {
		return mainActivity;
	}

	public void setMainActivity(Activity mainActivity) {
		this.mainActivity = mainActivity;
	}

	public String getBasePath() {
		return basePath;
	}

	/**
	 * 导出的gps轨迹数据
	 * @return
	 */
	public String getGpsPath() {
		return gpsPath;
	}

	/**
	 * 存储导入的数据
	 * @return
	 */
	public String getDataPath() {
		return dataPath;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public String getPhotoPath() {
		return photoPath;
	}

	public String getSmallPhotoPath() {
		return smallPhotoPath;
	}

	public GpsLocationService getGpsLocationService() {
		return gpsLocationService;
	}

	public SensorService getSensorService() {
		return sensorService;
	}

	public MapApplication() {
		initUserDataPath();
		initGdalOgr();
		gpsLocationService = new GpsLocationService();
		sensorService = new SensorService();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		//创建位置服务和方位服务
		gpsLocationService.create(getContext());
		sensorService.create(getContext());
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
	}

	private void initUserDataPath(){
		if (Environment.getExternalStorageDirectory().isDirectory()
				&& Environment.getExternalStorageDirectory().canRead()) {
			File path = Environment.getExternalStorageDirectory();
			File base = new File(path.getAbsolutePath() + basePath);
			if (!base.exists())
				base.mkdir();
			basePath=path.getAbsolutePath() + basePath;
			File data = new File(path.getAbsolutePath() + dataPath);
			if (!data.exists())
				data.mkdir();
			dataPath= path.getAbsolutePath()+dataPath;
			File output = new File(path.getAbsolutePath() + outputPath);
			if (!output.exists())
				output.mkdir();
			outputPath =path.getAbsolutePath()+ outputPath;
			File gps = new File(path.getAbsolutePath() + gpsPath);
			if (!gps.exists())
				gps.mkdir();
			gpsPath=path.getAbsolutePath()+gpsPath;
			File photo = new File(path.getAbsolutePath() + photoPath);
			if (!photo.exists())
				photo.mkdir();
			photoPath=path.getAbsolutePath()+photoPath;
			File smallPhoto = new File(path.getAbsolutePath() + smallPhotoPath);
			if (!photo.exists())
				smallPhoto.mkdir();
			smallPhotoPath=path.getAbsolutePath()+smallPhotoPath;
		}
	}

	private void initGdalOgr(){
		ogr.RegisterAll();
		// 为了支持中文路径，请添加下面这句代码
		gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "NO");
		// 为了使属性表字段支持中文，请添加下面这句
		gdal.SetConfigOption("SHAPE_ENCODING","");
	}
}
