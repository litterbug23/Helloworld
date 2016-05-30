package com.example.administrator.mapdev;

import android.location.Location;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * Created by Administrator on 2016/3/25.
 */
public class GpsLocation extends DataSupport {
	public double getLongitude() {
		return longitude;
	}

	public GpsLocation setLongitude(double longitude) {
		this.longitude = longitude;
		return this;
	}

	public double getLatitude() {
		return latitude;
	}

	public GpsLocation setLatitude(double latitude) {
		this.latitude = latitude;
		return this;
	}

	public double getAltitude() {
		return altitude;
	}

	public GpsLocation setAltitude(double altitude) {
		this.altitude = altitude;
		return this;
	}

	public float getSpeed() {
		return speed;
	}

	public GpsLocation setSpeed(float speed) {
		this.speed = speed;
		return this;
	}

	public long getDate() {
		return date;
	}

	public GpsLocation setDate(long date) {
		this.date = date;
		return this;
	}

	public String getRoute() {
		return route;
	}

	public GpsLocation setRoute(String route) {
		this.route = route;
		return this;
	}

	public GpsLocation(){}

	public GpsLocation(Location location){
		longitude=location.getLongitude();
		latitude=location.getLatitude();
		altitude=location.getAltitude();
		speed=location.getSpeed();
		date= location.getTime();
	}

	private double longitude;
	private double latitude;
	private double altitude;
	private float speed;
	private long date;
	private String route;
}
