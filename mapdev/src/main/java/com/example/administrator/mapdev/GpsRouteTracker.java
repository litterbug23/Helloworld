package com.example.administrator.mapdev;

import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

//import de.micromata.opengis.kml.v_2_2_0.Document;
//import de.micromata.opengis.kml.v_2_2_0.Folder;
//import de.micromata.opengis.kml.v_2_2_0.Icon;
//import de.micromata.opengis.kml.v_2_2_0.Kml;
//import de.micromata.opengis.kml.v_2_2_0.Placemark;
//import de.micromata.opengis.kml.v_2_2_0.Style;

/**
 * Created by Administrator on 2016/3/26.
 */
public class GpsRouteTracker {
	static private int GPS_TRACK_STATE_START = 1;
	static private int GPS_TRACK_STATE_STOP = 2;
	static private int GPS_TRACK_STATE_READY = 0;

	private int gpsTrackState = GPS_TRACK_STATE_READY;
	private String currentRouteName;
	private Date startDate;
	private Date endDate;
	private List<GpsLocation> gpsLocations;
	private GpsLocationService gpsLocationService;
	private LayersManager layersManager;
	private SimpleLineSymbol lineSymbol;
	private SimpleMarkerSymbol markerSymbol;
	private Point lastPoint;
	private Point nextPoint;
	private Graphic currentRoute = null;
	private Polyline currentRouteLine = null;
	private GraphicsLayer graphicsLayer;
	private MapView mapView;
	private LocationDisplayManager locationDisplayManager;
	private List<String> routeNameList;
	private HashMap<String, Integer> routeTrackerStates;

	public GpsRouteTracker(LayersManager layersManager) {
		lineSymbol = new SimpleLineSymbol(Color.GREEN, 3, SimpleLineSymbol.STYLE.SOLID);
		markerSymbol = new SimpleMarkerSymbol(Color.BLUE, 8, SimpleMarkerSymbol.STYLE.CIRCLE);
		mapView = layersManager.getMapView();
		locationDisplayManager = mapView.getLocationDisplayManager();
		graphicsLayer = layersManager.getDrawerLayer();
		this.layersManager = layersManager;
		routeNameList = loadAllRouteNames();
		routeTrackerStates = new HashMap<>();
	}

	public List<String> getRouteNameList() {
		return routeNameList;
	}

	public boolean isShowRouteTracker(String routeName) {
		if (routeTrackerStates.containsKey(routeName))
			return true;
		return false;
	}

	public void startRouteTracker() {
		if (gpsTrackState == GPS_TRACK_STATE_START) {
			Toast.makeText(MapApplication.getContext(), "已经开始轨迹记录，必须结束轨迹记录才能重新开始记录", Toast.LENGTH_SHORT).show();
			return;
		}
		gpsTrackState = GPS_TRACK_STATE_START;
		startDate = new Date();
		if (!locationDisplayManager.isStarted())
			locationDisplayManager.start();
		//gpsLocationService.addLocationListener(gpsLocationListener);
		gpsLocations = new ArrayList<>();
		locationDisplayManager.setLocationListener(locationListener);
		locationDisplayManager.setAutoPanMode(LocationDisplayManager.AutoPanMode.NAVIGATION);
		currentRouteLine = new Polyline();
		currentRoute = new Graphic(currentRouteLine, lineSymbol);
		graphicsLayer.addGraphic(currentRoute);
		lastPoint = null;
	}

	private void resetRouteTracker() {
		//gpsLocationService.removeLocationListener(gpsLocationListener);
		locationDisplayManager.setLocationListener(null);
		locationDisplayManager.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
		locationDisplayManager.stop();
	}

	/**
	 * 停止路径跟踪，不保存路径跟踪
	 */
	public void discardRouteTracker() {
		resetRouteTracker();
		gpsTrackState = GPS_TRACK_STATE_STOP;
		if (currentRoute != null) {
			currentRouteLine = null;
			if (currentRoute != null)
				graphicsLayer.removeGraphic(currentRoute.getUid());
			currentRoute = null;
			lastPoint = null;
		}
	}

	/**
	 * 停止路径跟踪，并保存路径跟踪
	 */
	public boolean stopRouteTracker() {
		if (gpsTrackState != GPS_TRACK_STATE_START) {
			Toast.makeText(MapApplication.getContext(), "必须开始轨迹记录,才能结束记录", Toast.LENGTH_SHORT).show();
			return false;
		}
		gpsTrackState = GPS_TRACK_STATE_STOP;
		resetRouteTracker();
		endDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yy.MM.dd.HH.mm.ss");
		currentRouteName = dateFormat.format(startDate) + "_" + dateFormat.format(endDate);
		routeNameList.add(currentRouteName);
		if (gpsLocations.size() > 1) {
			for (GpsLocation location : gpsLocations) {
				location.setRoute(currentRouteName);
			}
			DataSupport.saveAll(gpsLocations);
			return true;
		}
		return false;
		//exportKMLFile(currentRouteName);
	}

	public void loadAllRouteTracker() {
		List<String> routeNames = loadAllRouteNames();
		if (routeNames == null)
			return;
		for (String routeName : routeNames) {
			showRouteTracker(routeName);
		}
	}

	private LocationListener locationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			GpsLocation gpsLocation = new GpsLocation(location);
			gpsLocations.add(gpsLocation);
			updateRouteTracker(gpsLocation);
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

	private GpsLocationListener gpsLocationListener = new GpsLocationListener() {
		@Override
		public void onLocationChanged(GpsLocation location) {
			gpsLocations.add(location);
			updateRouteTracker(location);
		}
	};

	private void updateRouteTracker(GpsLocation location) {
		if (lastPoint == null) {
			lastPoint = layersManager.wgs84ToMapProject(location);
		} else {
			nextPoint = layersManager.wgs84ToMapProject(location);
			Line segment = new Line();
			segment.setStart(lastPoint);
			segment.setEnd(nextPoint);
			lastPoint = nextPoint;
			Polyline polyline = new Polyline();
			polyline.addSegment(segment, true);
			Graphic graphic = new Graphic(polyline, lineSymbol);
			graphicsLayer.addGraphic(graphic);
			//currentRouteLine.addSegment(segment, true);
			//graphicsLayer.updateGraphic(currentRoute.getUid(), currentRouteLine);
		}
	}

	public void showRouteTracker(String routeName) {
		//数据已经在地图显示，不需要再次在地图中调用
		if (routeTrackerStates.containsKey(routeName))
			return;
		List<GpsLocation> gpsLocationList = loadRouteTracker(routeName);
		if (gpsLocationList == null)
			return;
		if (gpsLocationList.size() < 2)
			return;
		Polyline polyline = new Polyline();
		Point startPoint = null;
		Point endPoint = null;
		for (int i = 1; i < gpsLocationList.size(); i++) {
			GpsLocation location = gpsLocationList.get(i - 1);
			GpsLocation nextLocation = gpsLocationList.get(i);
			startPoint = layersManager.wgs84ToMapProject(location);
			endPoint = layersManager.wgs84ToMapProject(nextLocation);
			Line line = new Line();
			line.setStart(startPoint);
			line.setEnd(endPoint);
			polyline.addSegment(line, false);
		}
		Graphic graphic = new Graphic(polyline, lineSymbol);
		int id = graphicsLayer.addGraphic(graphic);
		routeTrackerStates.put(routeName, id);
//		try {
//			com.esri.core.internal.util.d d=new d();
//			String json = Graphic.toJson(graphic);
//			String jsonSLS = lineSymbol.toJson();
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			ObjectOutputStream oos = new ObjectOutputStream(bos);
//			oos.writeObject(graphic);
//			oos.close();
//			byte[] bytes = bos.toByteArray();
//			java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
//			java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);      //something wrong here
//			Graphic tmp=(Graphic)ois.readObject();
//			Log.d("Test",tmp.toString());

//			SurveyDataManager surveyDataManager=new SurveyDataManager();
//			surveyDataManager.deleteAllSurveyData();
//			SurveyData surveyData = surveyDataManager.toSurveyData(graphic);
//			surveyDataManager.saveSurveyData(surveyData);
//			Graphic tmp = surveyDataManager.fromSurveyData(surveyData);
//			surveyDataManager.loadAllSurveyData();

//		} catch (Exception e) {
//			Log.d("SurveyData",e.getMessage());
//		}
	}

	/**
	 * 删除路径
	 *
	 * @param routeName
	 */
	public void removeRouteTracker(String routeName) {
		DataSupport.deleteAll(GpsLocation.class, "route = ?", routeName);
		this.routeNameList.remove(routeName);
		closeRouteTracker(routeName);
	}

	public void closeRouteTracker(String routeName) {
		if (routeTrackerStates.containsKey(routeName)) {
			graphicsLayer.removeGraphic(routeTrackerStates.get(routeName));
			routeTrackerStates.remove(routeName);
		}
	}

	static void deleteRouteTracker(String routeName) {
		DataSupport.deleteAll(GpsLocation.class, "route = ?", routeName);
	}

	/**
	 * 获得所有gps轨迹的名称信息
	 *
	 * @return
	 */
	static public List<String> loadAllRouteNames() {
		Cursor cursor = DataSupport.findBySQL("select route from GpsLocation group by route");
		List<String> routeNames = new ArrayList<>();
		while (cursor.moveToNext()) {
			routeNames.add(cursor.getString(0));
		}
		return routeNames;
	}

	/**
	 * 读取一段路径跟踪
	 *
	 * @param routeName
	 * @return
	 */
	static public List<GpsLocation> loadRouteTracker(String routeName) {
		List<GpsLocation> routeTracker = DataSupport.where("route = ?", routeName).order("date desc").find(GpsLocation.class);
		return routeTracker;
	}

	static public List<GpsLocation> loadRouteTrackerByDate(Date startTime, Date endTime) {
		List<GpsLocation> routeTracker = DataSupport.
				where("date>=? and date <= ?", String.valueOf(startTime.getTime()), String.valueOf(endTime.getTime())).
				order("date desc").find(GpsLocation.class);
		return routeTracker;
	}

	/**
	 * 导出GPX标准的轨迹数据
	 *
	 * @param routeName
	 * @return
	 */
	static public boolean exportGPXFile(String routeName) {
		List<GpsLocation> routeTrackers = loadRouteTracker(routeName);

		return true;
	}

	/**
	 * 导出kml格式的轨迹数据
	 *
	 * @param routeName
	 * @return
	 */
	static public boolean exportKMLFile(String routeName) {
//		List<GpsLocation> routeTrackers = loadRouteTracker(routeName);
//		if (routeTrackers == null)
//			return false;
//		String kmlName = MapApplication.instance().getGpsPath() + "/" + routeName + ".kml";
//		final Kml kml = new Kml();
//		Document doc = kml.createAndSetDocument().withName("Route").withOpen(true);
//		// create a Folder
//		Folder folder = doc.createAndAddFolder();
//		folder.withName(routeName).withOpen(true);
//		for (GpsLocation location : routeTrackers) {
//			createPlacemarkWithChart(doc, folder, location.getLatitude(), location.getLongitude(), routeName, 10);
//		}
//		// print and save
//		try {
//			kml.marshal(new File(kmlName));
//		} catch (FileNotFoundException e) {
//		}
		return true;
	}

//	private static void createPlacemarkWithChart(Document document, Folder folder, double longitude, double latitude,
//	                                             String continentName, int coveredLandmass) {
//
//		int remainingLand = 100 - coveredLandmass;
//		Icon icon = new Icon()
//				.withHref("http://chart.apis.google.com/chart?chs=380x200&chd=t:" + coveredLandmass + "," + remainingLand + "&cht=p&chf=bg,s,ffffff00");
//		Style style = document.createAndAddStyle();
//		style.withId("style_" + continentName) // set the stylename to use this style from the placemark
//				.createAndSetIconStyle().withScale(5.0).withIcon(icon); // set size and icon
//		style.createAndSetLabelStyle().withColor("ff43b3ff").withScale(5.0); // set color and size of the continent name
//
//		Placemark placemark = folder.createAndAddPlacemark();
//		// use the style for each continent
//		placemark.withName(continentName)
//				.withStyleUrl("#style_" + continentName)
//						// 3D chart imgae
//				.withDescription(
//						"<![CDATA[<img src=\"http://chart.apis.google.com/chart?chs=430x200&chd=t:" + coveredLandmass + "," + remainingLand + "&cht=p3&chl=" + continentName + "|remaining&chtt=Earth's surface\" />")
//						// coordinates and distance (zoom level) of the viewer
//				.createAndSetLookAt().withLongitude(longitude).withLatitude(latitude).withAltitude(0).withRange(12000000);
//
//		placemark.createAndSetPoint().addToCoordinates(longitude, latitude); // set coordinates
//	}
}
