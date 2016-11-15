package com.example.administrator.mapdev;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * Created by Administrator on 2016/3/21.
 */
public class PhotoSurvey extends DataSupport {

	private String photoImage;
	private long date;
	private double longitude;
	private double latitude;
	private double altitude;
	private double azimuth;
	private String staff;
	private String comment;
	private MapScene mapScene;	//照片采集也与地图关联，采集后的照片将按照地图进行组织

	public MapScene getMapScene() {
		return mapScene;
	}

	public void setMapScene(MapScene mapScene) {
		this.mapScene = mapScene;
	}


	/**
	 * 采集日期
	 *
	 * @return
	 */
	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public void setDate(Date date) {
		this.date = date.getTime();
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	/**
	 * 采集方位角(从正北方向开始0~360度）
	 *
	 * @return
	 */
	public double getAzimuth() {
		return azimuth;
	}

	public void setAzimuth(double azimuth) {
		this.azimuth = azimuth;
	}

	/**
	 * 采集人员
	 *
	 * @return
	 */
	public String getStaff() {
		return staff;
	}

	public void setStaff(String staff) {
		this.staff = staff;
	}

	public long getId() {
		return super.getBaseObjId();
	}

	/**
	 * 备注信息
	 *
	 * @return
	 */
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * 采集照片的名称
	 * 默认采集照片不再包含照片路劲
	 * @return
	 */
	public String getPhotoImage() {
		return photoImage;
	}

	public void setPhotoImage(String photoImage) {
		this.photoImage = photoImage;
	}
}
