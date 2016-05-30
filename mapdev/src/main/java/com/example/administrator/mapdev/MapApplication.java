package com.example.administrator.mapdev;

import android.content.res.Configuration;
import android.os.Environment;

import org.litepal.LitePalApplication;

import java.io.File;

/**
 * Created by Administrator on 2016/3/26.
 */
public class MapApplication extends LitePalApplication {
	private GpsLocationService gpsLocationService;
	private SensorService sensorService;
	private String basePath = "/mapdev";
	private String gpsPath="/mapdev/gps";
	private String dataPath="/mapdev/data";
	private String outputPath ="/mapdev/output";
	private String photoPath = "/mapdev/output/photo";
	private String smallPhotoPath="/mapdev/output/smallPhoto";

	static public MapApplication instance(){
		return (MapApplication)getContext();
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
			File gps = new File(path.getAbsolutePath() + gpsPath);
			if (!gps.exists())
				gps.mkdir();
			gpsPath=path.getAbsolutePath()+gpsPath;
			File data = new File(path.getAbsolutePath() + outputPath);
			if (!data.exists())
				data.mkdir();
			outputPath =path.getAbsolutePath()+ outputPath;
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
}
