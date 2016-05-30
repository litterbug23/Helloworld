package com.example.administrator.mapdev;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by Administrator on 2016/3/25.
 */
public class GpsLocationService {
	static private int STATE_NULL = 0;
	static private int STATE_CREATED = 1;
	static private int STATE_RESUME = 2;
	static private int STATE_PAUSE = 3;
	private int locationState = STATE_NULL;
	private LocationManager locationManager;
	private Context context;
	private String providerName;
	private Location currentBestLocation;
	private boolean locationAvailable=false;
	private List<GpsLocationListener> gpsLocationListenerList=new ArrayList<>();

	public void create(Context context) {
		if (locationState == STATE_NULL) {
			this.context = context;
			locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			locationState = STATE_CREATED;
		}
	}

	public void resume() {
		if (locationState == STATE_CREATED || locationState == STATE_PAUSE) {
			// 查找到服务信息
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
			criteria.setAltitudeRequired(true);
			criteria.setSpeedRequired(true);
			criteria.setBearingRequired(false);
			criteria.setCostAllowed(true);
			criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
			String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息
			try {
				Location lastKnowLocation;  //上次定位的缓存数据
				boolean gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
				if (gpsEnable) {
					//获得上次定位的缓存数据
					lastKnowLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
					if (isBetterLocation(lastKnowLocation, currentBestLocation))
						currentBestLocation = lastKnowLocation;
				}
				boolean networkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
				if (networkEnable) {
					lastKnowLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					locationManager.requestLocationUpdates(provider, 0, 0, networkListener);
					if (isBetterLocation(lastKnowLocation, currentBestLocation))
						currentBestLocation = lastKnowLocation;
				}
				locationState = STATE_RESUME;
			} catch (SecurityException e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}

	public void pause() {
		if (locationState == STATE_RESUME) {
			try {
				locationManager.removeUpdates(locationListener);
				locationManager.removeUpdates(networkListener);
				locationState = STATE_PAUSE;
				locationAvailable=false;
			} catch (SecurityException e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}

	public boolean isLocationAvailable() {
		return locationAvailable;
	}

	/**
	 * 获得当前GPS服务状态
	 * @return
	 */
	public int getLocationState(){
		return  locationState;
	}

	public Location getCurrentBestLocation() {
		return currentBestLocation;
	}

	public String getProviderName() {
		return providerName;
	}

	public void addLocationListener(GpsLocationListener listener){
		synchronized(gpsLocationListenerList){
			gpsLocationListenerList.add(listener);
		}
	}

	public void removeLocationListener(GpsLocationListener listener){
		synchronized(gpsLocationListenerList){
			gpsLocationListenerList.remove(listener);
		}
	}

	public void updateLocation(Location location) {
		synchronized(gpsLocationListenerList){
			for(GpsLocationListener listener : gpsLocationListenerList){
				GpsLocation gpsLocation=new GpsLocation(location);
				listener.onLocationChanged(gpsLocation);
			}
		}
	}

	//没有GPS信号时采用网络定位
	final LocationListener networkListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			boolean flag = isBetterLocation(location,
					currentBestLocation);
			if (flag) {
				currentBestLocation = location;
				locationAvailable=true;
				updateLocation(location);
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {
			providerName = provider;
		}

		@Override
		public void onProviderDisabled(String provider) {
		}
	};

	//有GPS信号采用GPS定位，关闭网络定位
	final LocationListener locationListener = new LocationListener() {
		private boolean isRemove = false;//判断网络监听是否移除

		@Override
		public void onLocationChanged(Location location) {
			boolean flag = isBetterLocation(location,
					currentBestLocation);
			if (flag) {
				currentBestLocation = location;
				locationAvailable=true;
				updateLocation(location);
			}
			// 获得GPS服务后，移除network监听
			if (location != null && !isRemove) {
				try {
					locationManager.removeUpdates(networkListener);
					isRemove = true;
				} catch (SecurityException e) {
					Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (LocationProvider.OUT_OF_SERVICE == status) {
				Toast.makeText(context, "GPS服务丢失,切换至网络定位",
						Toast.LENGTH_SHORT).show();
			} else {
				try {
					locationManager
							.requestLocationUpdates(
									LocationManager.NETWORK_PROVIDER, 0, 0,
									networkListener);
				} catch (SecurityException e) {
					Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
			providerName = provider;
		}

		@Override
		public void onProviderDisabled(String provider) {
		}
	};

	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 *
	 * @param location            The new Location that you want to evaluate
	 * @param currentBestLocation The current Location fix, to which you want to compare the new
	 *                            one
	 */
	protected boolean isBetterLocation(Location location,
	                                   Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/**
	 * Checks whether two providers are the same
	 */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
}
