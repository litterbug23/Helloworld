package com.example.administrator.sensortest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
	private SensorManager sm;
	//需要两个Sensor
	private Sensor aSensor;
	private Sensor mSensor;
	private LocationManager locationManager;
	private String TAG="test";
	float[] accelerometerValues = new float[3];
	float[] magneticFieldValues = new float[3];
	private TextView heading;
	private TextView azimuth;
	private TextView pitch;
	private TextView roll;
	private TextView x,y,z;
	private TextView gps_x,gps_y,gps_z;
	public MainActivityFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View   view = inflater.inflate(R.layout.fragment_main, container, false);
		heading=(TextView)view.findViewById(R.id.heading);
		azimuth=(TextView)view.findViewById(R.id.azimuth);
		pitch=(TextView)view.findViewById(R.id.pitch);
		roll=(TextView)view.findViewById(R.id.roll);
		x=(TextView)view.findViewById(R.id.x);
		y=(TextView)view.findViewById(R.id.y);
		z=(TextView)view.findViewById(R.id.z);
		gps_x=(TextView)view.findViewById(R.id.gps_x);
		gps_y=(TextView)view.findViewById(R.id.gps_y);
		gps_z=(TextView)view.findViewById(R.id.gps_z);

		sm = (SensorManager)this.getContext().getSystemService(Context.SENSOR_SERVICE);
		aSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		String serviceName = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) this.getContext().getSystemService(serviceName);


		return view;
	}

	@Override
	public void onResume() {
		sm.registerListener(myListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sm.registerListener(myListener, mSensor,SensorManager.SENSOR_DELAY_NORMAL);
		//更新显示数据的方法
		calculateOrientation();

		// 查找到服务信息
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
		criteria.setAltitudeRequired(true);
		criteria.setSpeedRequired(true);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false);
		criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
		String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息
		boolean gpsEnable=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean networkEnable=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		//((TextView)view.findViewById(R.id.gps)).setText(provider);
		try {
			Location location = locationManager.getLastKnownLocation(provider);
			if(location!=null) {
				gps_x.setText(String.format(" %.6f",location.getLongitude()));
				gps_y.setText(String.format(" %.6f",location.getLatitude()));
				gps_z.setText(location.toString());
			}
			locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
		}catch(SecurityException e){
			Toast.makeText(this.getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
		}

		super.onResume();
	}

	@Override
	public void onPause() {
		sm.unregisterListener(myListener);
		try {
			locationManager.removeUpdates(locationListener);
		}catch (SecurityException e){
			Toast.makeText(this.getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
		}
		super.onPause();
	}

	final LocationListener locationListener=new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			gps_x.setText(String.format(" %.6f",location.getLongitude()));
			gps_y.setText(String.format(" %.6f",location.getLatitude()));
			gps_z.setText(location.toString());
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}
	};

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
		float[] values = new float[3];
		float[] R = new float[9];
		SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
		float[] DestR=new float[9];
		SensorManager.remapCoordinateSystem(R,SensorManager.AXIS_X,SensorManager.AXIS_Z,DestR);
		SensorManager.getOrientation(DestR, values);

		x.setText( String.valueOf(magneticFieldValues[0]) );
		y.setText(String.valueOf(magneticFieldValues[1]));
		z.setText(String.valueOf(magneticFieldValues[2]));

		// 要经过一次数据格式的转换，转换为度
		values[0] = (float) Math.toDegrees(values[0]);
		Log.i(TAG, values[0]+"");
		values[1] = (float) Math.toDegrees(values[1]);
		values[2] = (float) Math.toDegrees(values[2]);
		azimuth.setText( String.valueOf(values[0]) );
		pitch.setText( String.valueOf(values[1]) );
		roll.setText( String.valueOf(values[2]) );

		if(values[0] >= -5 && values[0] < 5){
			Log.i(TAG, "正北");
			heading.setText("正北");
		}
		else if(values[0] >= 5 && values[0] < 85){
			Log.i(TAG, "东北");
			heading.setText("东北");
		}
		else if(values[0] >= 85 && values[0] <=95){
			Log.i(TAG, "正东");
			heading.setText("正东");
		}
		else if(values[0] >= 95 && values[0] <175){
			Log.i(TAG, "东南");
			heading.setText("东南");
		}
		else if((values[0] >= 175 && values[0] <= 180) || (values[0]) >= -180 && values[0] < -175){
			Log.i(TAG, "正南");
			heading.setText("正南");
		}
		else if(values[0] >= -175 && values[0] <-95){
			Log.i(TAG, "西南");
			heading.setText("西南");
		}
		else if(values[0] >= -95 && values[0] < -85){
			Log.i(TAG, "正西");
			heading.setText("正西");
		}
		else if(values[0] >= -85 && values[0] <-5){
			Log.i(TAG, "西北");
			heading.setText("西北");
		}

	}
}
