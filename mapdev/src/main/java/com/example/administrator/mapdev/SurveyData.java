package com.example.administrator.mapdev;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * Created by Administrator on 2016/4/22.
 */
public class SurveyData extends DataSupport{

	private int drawOrder;
	private int WKID;
	private Date surveyDate;
	private String description;
	private String staff;
	private byte[] symbolStyle;
	private byte[] geometry;
	private byte[] attributes;

	@Override
	public long getBaseObjId() {
		return super.getBaseObjId();
	}

	public int getDrawOrder() {
		return drawOrder;
	}

	public SurveyData setDrawOrder(int drawOrder) {
		this.drawOrder = drawOrder;
		return this;
	}

	public int getWKID() {
		return WKID;
	}

	public SurveyData setWKID(int WKID) {
		this.WKID = WKID;
		return this;
	}

	public Date getSurveyDate() {
		return surveyDate;
	}

	public SurveyData setSurveyDate(Date surveyDate) {
		this.surveyDate = surveyDate;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public String getStaff() {
		return staff;
	}

	public void setStaff(String staff) {
		this.staff = staff;
	}

	public SurveyData setDescription(String description) {
		this.description = description;
		return this;
	}

	public byte[] getSymbolStyle() {
		return symbolStyle;
	}

	public SurveyData setSymbolStyle(byte[] symbolStyle) {
		this.symbolStyle = symbolStyle;
		return this;
	}

	public byte[] getGeometry() {
		return geometry;
	}

	public SurveyData setGeometry(byte[] geometry) {
		this.geometry = geometry;
		return this;
	}

	public byte[] getAttributes() {
		return attributes;
	}

	public SurveyData setAttributes(byte[] attributes) {
		this.attributes = attributes;
		return this;
	}

}
