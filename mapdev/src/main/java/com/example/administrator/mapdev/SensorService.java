package com.example.administrator.mapdev;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.text.NumberFormat;

/**
 * Created by Administrator on 2016/3/25.
 */
public class SensorService {
	static private String TAG="Sensor";
	static private int STATE_NULL=0;
	static private int STATE_CREATED=1;
	static private int STATE_RESUME=2;
	static private int STATE_PAUSE=3;
	private int sensorState=STATE_NULL;
	private Context context;
	private SensorManager sm;
	//需要两个Sensor
	private Sensor aSensor;
	private Sensor mSensor;
	private float[] accelerometerValues = new float[3];
	private float[] magneticFieldValues = new float[3];
	private float[] orientation =new float[3];    //正常地图的朝向
	private float[] cameraOrientation =new float[3];  //相机拍摄的朝向

	public void create(Context context){
		if(sensorState==STATE_NULL) {
			sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
			aSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			sensorState=STATE_CREATED;
		}
	}

	public void resume(){
		if(sensorState==STATE_CREATED || sensorState==STATE_PAUSE) {
			sensorState=STATE_RESUME;
			sm.registerListener(myListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
			sm.registerListener(myListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	public void pause(){
		if(sensorState==STATE_RESUME) {
			sm.unregisterListener(myListener);
			sensorState=STATE_PAUSE;
		}
	}

	public int getSensorState(){
		return sensorState;
	}

	final SensorEventListener myListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent sensorEvent) {

			if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
				magneticFieldValues = sensorEvent.values;
			if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
				accelerometerValues = sensorEvent.values;
			calculateOrientation();
		}
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	};

	private  void calculateOrientation() {
		float[] R = new float[9];
		SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
		//计算地图的朝向
		SensorManager.getOrientation(R, orientation);
		//计算相机的拍摄的朝向
		float[] DestR=new float[9];
		//必须重新映射坐标系（使用绕Y轴旋转，计算摄像头的朝向）
		SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, DestR);
		SensorManager.getOrientation(DestR, cameraOrientation);
	}

	/**
	 * 获得相机拍摄方向的方位角
	 * @return 方位角的范围(-180,180)，逆时针方向（正北0°，往东正值，往西负值)
	 */
	public double getLastKnowCameraAzimuth(){
		resume();
		return Math.toDegrees(cameraOrientation[0]);
	}

	/**
	 * 获得地图的方位角
	 * @return 方位角的范围(-180,180)，逆时针方向（正北0°，往东正值，往西负值)
	 */
	public double getLastKnowAzimuth(){
		resume();
		return Math.toDegrees(orientation[0]);
	}

	static public String getAzimuthString(double azimuth) {
		String azimuthString=null;
		if(azimuth >= -5 && azimuth < 5){
			azimuthString=("正北");
		}
		else if(azimuth >= 5 && azimuth < 85){
			azimuthString=("东北");
		}
		else if(azimuth >= 85 && azimuth <=95){
			azimuthString=("正东");
		}
		else if(azimuth >= 95 && azimuth <175){
			azimuthString=("东南");
		}
		else if((azimuth >= 175 && azimuth <= 180) || (azimuth) >= -180 && azimuth < -175){
			azimuthString=("正南");
		}
		else if(azimuth >= -175 && azimuth <-95){
			azimuthString=("西南");
		}
		else if(azimuth >= -95 && azimuth < -85){
			azimuthString=("正西");
		}
		else if(azimuth >= -85 && azimuth <-5){
			azimuthString=("西北");
		}
		String degree= String.format(" %.1f",azimuth);
		azimuthString += degree;
		return azimuthString;
	}
	
	/**
	 *将角度转化为比较容易理解的描述信息,比如azimuth=90，用正东方向更好理解，比如azimuth=45，用北偏东45°更好理解
	 * @param azimuth
	 * @return
	 */
	static public String getCompassDescription(double azimuth){
		if(azimuth >= -5 && azimuth < 5){
			Log.i(TAG, "正北");
			return "正北";
		}
		else if(azimuth >= 5 && azimuth < 85){
			Log.i(TAG, "东北");
			return  "东北";
		}
		else if(azimuth >= 85 && azimuth <=95){
			Log.i(TAG, "正东");
			return  "正东";
		}
		else if(azimuth >= 95 && azimuth <175){
			Log.i(TAG, "东南");
			return  "东南";
		}
		else if((azimuth >= 175 && azimuth <= 180) || (azimuth) >= -180 && azimuth < -175){
			Log.i(TAG, "正南");
			return  "正南";
		}
		else if(azimuth >= -175 && azimuth <-95){
			Log.i(TAG, "西南");
			return  "西南";
		}
		else if(azimuth >= -95 && azimuth < -85){
			Log.i(TAG, "正西");
			return  "正西";
		}
		else{
//			if(azimuth >= -85 && azimuth <-5)
			Log.i(TAG, "西北");
			return  "西北";
		}
	}
}
