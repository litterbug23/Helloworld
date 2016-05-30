package com.example.administrator.mapdev;

import android.util.Log;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.toolkit.analysis.MeasuringTool;
import com.esri.core.geometry.Geometry;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.Symbol;

import org.litepal.crud.DataSupport;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/22.
 * 数据管理方式与PhotoSurvey类似
 * 1、新建数据后，直接存入数据库中
 * 2、从数据库中加载数据，排除当前已经加载的数据
 */
public class SurveyDataManager {
	private Map<Long, SurveyData> photoSurveyMap = new LinkedHashMap<>();
	private GraphicsLayer graphicsLayer;

	/**
	 * 将graphic数据转换成SurveyData格式
	 *
	 * @param graphic
	 */
	public SurveyData toSurveyData(Graphic graphic) {
		if (graphic == null || graphic.getGeometry() == null)
			return null;
		SurveyData surveyData = new SurveyData();
		try {
			{
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos));
				oos.writeObject(graphic.getGeometry());
				oos.close();
				byte[] bytes = bos.toByteArray();
				bos.close();
				surveyData.setGeometry(bytes);
			}
			if (graphic.getSymbol() != null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos));
				bos.reset();
				oos.reset();
				oos.writeObject(graphic.getSymbol());
				oos.close();
				byte[] bytes = bos.toByteArray();
				bos.close();
				surveyData.setSymbolStyle(bytes);
			}
			if (graphic.getAttributes() != null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos));
				bos.reset();
				oos.reset();
				oos.writeObject(graphic.getAttributes());
				oos.close();
				byte[] bytes = bos.toByteArray();
				bos.close();
				surveyData.setAttributes(bytes);
			}
			surveyData.setDrawOrder(graphic.getDrawOrder());
			surveyData.setSurveyDate(new Date());
		} catch (IOException e) {
			Log.d("SurveyData", e.getMessage());
		}

		return surveyData;
	}

	public Graphic fromSurveyData(SurveyData data) {
		if (data == null || data.getGeometry() == null)
			return null;
		try {
			Symbol symbol = null;
			Map<String, Object> attributes = null;
			Geometry geometry =null;
			{
				java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(data.getGeometry());
				java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new BufferedInputStream(bais));      //something wrong here
				geometry = (Geometry) ois.readObject();
				ois.close();
				bais.close();
			}
			if (data.getSymbolStyle() != null) {
				java.io.ByteArrayInputStream symbol_oais = new java.io.ByteArrayInputStream(data.getSymbolStyle());
				java.io.ObjectInputStream symbol_ois = new java.io.ObjectInputStream(new BufferedInputStream(symbol_oais));      //something wrong here
				symbol = (Symbol) symbol_ois.readObject();
				symbol_oais.close();
				symbol_ois.close();
			}
			if (data.getAttributes() != null) {
				java.io.ByteArrayInputStream attr_oais = new java.io.ByteArrayInputStream(data.getAttributes());
				java.io.ObjectInputStream attr_ois = new java.io.ObjectInputStream(new BufferedInputStream(attr_oais));      //something wrong here
				attributes = (Map<String, Object>) attr_ois.readObject();
				attr_ois.close();
				attr_oais.close();
			}
			Graphic graphic = new Graphic(geometry, symbol, attributes, data.getDrawOrder());
			return graphic;
		} catch (StreamCorruptedException e) {
			Log.d("SurveyData", e.getMessage());
		}catch (IOException e){
			Log.d("SurveyData", e.getMessage());
		}catch (ClassNotFoundException e){
			Log.d("SurveyData", e.getMessage());
		}

		return null;
	}

	/**
	 * 保存数据到数据库中
	 *
	 * @param data
	 */
	public void saveSurveyData(SurveyData data) {
		if (data == null)
			return;
		data.saveFast();
		photoSurveyMap.put(data.getBaseObjId(), data);
	}

	/**
	 * 如果数据修改，更新新的修改到数据库中
	 *
	 * @param data
	 */
	public void updateSurveyData(SurveyData data) {
		if (data == null)
			return;
		data.update(data.getBaseObjId());
	}

	public void loadAllSurveyData() {
		List<SurveyData> dbSurveyDataList = DataSupport.findAll(SurveyData.class);
		int progressInt = 0;
		for (SurveyData surveyData : dbSurveyDataList) {
			progressInt++;
			if (!photoSurveyMap.containsKey(surveyData.getBaseObjId())) {
				photoSurveyMap.put(surveyData.getBaseObjId(), surveyData);
				fromSurveyData(surveyData);
			}
		}
	}

	public void deleteAllSurveyData(){
		DataSupport.deleteAll(SurveyData.class);
	}

}
