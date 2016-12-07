package com.example.administrator.mapdev;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.map.Field;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.example.administrator.mapdev.tools.GeometryUtils;

import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;
import org.litepal.LitePalApplication;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/22.
 * 创建管理维护照片信息
 * 照片绘制将由PhotoSurveyLayer负责
 */
public class PhotoSurveyManager {

    public static String TAG = "PhotoSurveyManager";
    private GraphicsLayer userDrawerLayer;
    private LayersManager layersManager;
    private Context context = LitePalApplication.getContext();
    private GpsLocationService gpsLocationService;
    private SensorService sensorService;
    private Map<Long, PhotoSurvey> photoSurveyMap = new HashMap<>();
    protected List<Field> fields;

    //private List<PhotoSurvey> photoSurveyList=null;
    public PhotoSurveyManager(LayersManager layersManager, GpsLocationService gpsLocationService, SensorService sensorService) {
        initPhotoSurveyDrawer(layersManager, gpsLocationService, sensorService);
        initFields();
    }

    protected void initPhotoSurveyDrawer(LayersManager layersManager, GpsLocationService gpsLocationService, SensorService sensorService) {
        this.layersManager = layersManager;
        this.gpsLocationService = gpsLocationService;
        this.sensorService = sensorService;
    }

    /**
     * 初始化字段信息（照片的字段信息）
     */
    protected void initFields() {
        //TODO: 暂时固定属性字段
        fields = new ArrayList<>();
        //photoImage;azimuth;altitude;date;staff; comment;
        try {
            Field field = new Field("photoImage", "照片名称", Field.toEsriFieldType(Field.esriFieldTypeString));
            fields.add(field);
            field = new Field("azimuth", "方位角", Field.toEsriFieldType(Field.esriFieldTypeSingle));
            fields.add(field);
            field = new Field("altitude", "高程", Field.toEsriFieldType(Field.esriFieldTypeSingle));
            fields.add(field);
            field = new Field("date", "采集日期", Field.toEsriFieldType(Field.esriFieldTypeDate));
            fields.add(field);
            field = new Field("staff", "采集人员", Field.toEsriFieldType(Field.esriFieldTypeString));
            fields.add(field);
            field = new Field("comment", "描述信息", Field.toEsriFieldType(Field.esriFieldTypeString));
            fields.add(field);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    /**
     * 获得采集字段（属性编辑等需要使用）
     *
     * @return
     */
    public List<Field> getFields() {
        return fields;
    }

    public Location getLocation() {
        //return gpsLocationService.getCurrentBestLocation();
        return this.layersManager.getMapView().getLocationDisplayManager().getLocation();
    }

    /**
     * 加载所有的照片及相关数据
     */
    public void loadPhotoSurveyData(@Nullable ProgressBar progressBar) {
        List<PhotoSurvey> dbPhotoSurveyList = DataSupport.findAll(PhotoSurvey.class);
        int size = dbPhotoSurveyList.size();
        int progressInt = 0;
        double sizePercent = size != 0 ? (100.0 / (double) size) : 0;
        for (PhotoSurvey photoSurvey : dbPhotoSurveyList) {
            progressInt++;
            if (!photoSurveyMap.containsKey(photoSurvey.getId())) {
                photoSurveyMap.put(photoSurvey.getId(), photoSurvey);
                showPhotoMark(photoSurvey);
            }
            if (progressBar != null) {
                progressBar.setProgress((int) Math.floor(sizePercent * progressInt));
            }
        }
    }

    public void asyncLoadPhotoSurveyData(@Nullable ProgressBar progressBar) {
        AsyncLoadPhotoSurvey asyncWorker = new AsyncLoadPhotoSurvey(progressBar);
        asyncWorker.execute();
    }

    public double getCameraAzimuth() {
        return sensorService.getLastKnowCameraAzimuth();
    }

    /**
     * 采集照片数据，并将相关数据写入数据库中
     *
     * @param imagePath
     * @param location
     * @param comment
     */
    public void takePhotoAction(String imagePath, Location location, String comment) {
        MapScene mapScene = getCurrentScene();
        if (mapScene == null) {
            MapApplication.showMessage("当前没有打开地图，不能进行照片采集");
            return;
        }
        double azimuth = getCameraAzimuth();
        PhotoSurvey photoSurvey = new PhotoSurvey();
        photoSurvey.setLongitude(location.getLongitude());
        photoSurvey.setLatitude(location.getLatitude());
        photoSurvey.setAltitude(location.getAltitude());
        photoSurvey.setAzimuth(azimuth);
        photoSurvey.setComment(comment);
        photoSurvey.setPhotoImage(imagePath);
        photoSurvey.setStaff(mapScene.getUserName());
        Date now = new Date();
        photoSurvey.setDate(now);
        photoSurvey.setMapScene(mapScene);
        //保存数据到数据库
        photoSurvey.save();
        photoSurveyMap.put(photoSurvey.getId(), photoSurvey);
        //在地图显示拍摄照片的缩略图
        showPhotoMark(photoSurvey);
    }

    public void takePhotoAction(String imagePath, String comment) {
        //创建位置服务和方向服务
        Location location = layersManager.getCalibrateLocation();
        if (location == null) {
            Toast.makeText(context, "未获得GPS或者网络定位信号", Toast.LENGTH_SHORT).show();
            return;
        }
        takePhotoAction(imagePath, location, comment);
    }

    public static String getEncoding(String str) {
        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s = encode;
                return s;
            }
        } catch (Exception exception) {
        }
        encode = "ISO-8859-1";
        try {

            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s1 = encode;
                return s1;
            }
        } catch (Exception exception1) {
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s2 = encode;
                return s2;
            }
        } catch (Exception exception2) {
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s3 = encode;
                return s3;
            }
        } catch (Exception exception3) {
        }
        return "";
    }

    /**
     * 将采集点照片数据转换为OGR格式数据
     *
     * @param photoSurvey 照片数据
     * @param oFeature    填充Feature
     * @return 是否填充Feature成功
     */
    public boolean photoSurveyToOGRFeature(PhotoSurvey photoSurvey, Feature oFeature) {
        //转换属性信息
        String photoPath=photoSurvey.getPhotoImage();
        String encoding = getEncoding(photoPath);
        try {
            photoPath = new String(photoPath.getBytes(encoding), "UTF-8");
        }catch (UnsupportedEncodingException e) {
            Log.d(TAG,e.getMessage());
        }
        oFeature.SetField("photoImage", photoPath);
        oFeature.SetField("azimuth", photoSurvey.getAzimuth());
        oFeature.SetField("staff", photoSurvey.getStaff());
        oFeature.SetField("comment", photoSurvey.getComment());
        Date date = photoSurvey.getDate();
        oFeature.SetField("date", date.getYear(), date.getMonth(), date.getDay(),
                date.getHours(), date.getMinutes(), date.getSeconds(), 8);
        //转换图形信息
        Point geometry = new Point(photoSurvey.getLongitude(), photoSurvey.getLatitude());
        geometry = layersManager.wgs84ToMapProject(geometry);
        String wkt = GeometryUtils.GeometryToWKT(geometry);
        org.gdal.ogr.Geometry geom = org.gdal.ogr.Geometry.CreateFromWkt(wkt);
        if (geom == null)
            return false;
        oFeature.SetGeometry(geom);
        return true;
    }

    /**
     * 导出照片采集数据到指定目录
     */
    public boolean exportPhotoSurveyData() {
        MapScene mapScene = getCurrentScene();
        int sceneId = mapScene.getId();
        List<PhotoSurvey> dbPhotoSurveyList = DataSupport.where("mapScene_id = ?", String.valueOf(sceneId)).
                find(PhotoSurvey.class);
        if (dbPhotoSurveyList == null)
            return false;
        //获得投影坐标系
        com.esri.core.geometry.SpatialReference spatialRef = layersManager.getMapView().getSpatialReference();
        if (spatialRef == null)
            return false;
        //导出数据根目录
        String outputPath = MapApplication.instance().getOutputPath();
        String sceneName = mapScene.getSceneName();
        File base = new File(outputPath + "/" + sceneName);
        if (!base.exists())
            base.mkdir();
        //导出shp文件名称
        String strVectorFile = base.getAbsolutePath() + "/采集照片.shp";
        // 为了支持中文路径，请添加下面这句代码
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "NO");
        // 为了使属性表字段支持中文，请添加下面这句
        gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8");
        //创建数据，这里以创建ESRI的shp文件为例
        String strDriverName = "ESRI Shapefile";
        org.gdal.ogr.Driver oDriver = ogr.GetDriverByName(strDriverName);
        if (oDriver == null) {
            Log.d(TAG, strVectorFile + " 驱动不可用！\n");
            return false;
        }
        //如果存在该数据，则删除数据
        DataSource oDS = oDriver.Open(strVectorFile);
        if (oDS != null) {
            oDriver.DeleteDataSource(strVectorFile);
        }
        // 创建数据源
        oDS = oDriver.CreateDataSource(strVectorFile, null);
        if (oDS == null) {
            Log.d(TAG, "创建矢量文件【" + strVectorFile + "】失败！\n");
            return false;
        }
        String wkt = spatialRef.getText();
        // 创建图层，创建一个多边形图层，这里没有指定空间参考，如果需要的话，需要在这里进行指定
        SpatialReference sr = new SpatialReference(wkt); //osr.SRS_WKT_WGS84
        Layer oLayer = oDS.CreateLayer("PhotoSurvey", sr, 1, null); //1 表示点图层
        if (oLayer == null) {
            Log.d(TAG, "图层创建失败！\n");
            return false;
        }
        // 下面创建属性表
        for (Field field : fields) {
            int ogrFieldType = SurveyDataManager.esriFieldToOgrFieldType.get(field.getFieldType());
            String fieldName = field.getName();
            FieldDefn oField = new FieldDefn(fieldName, ogrFieldType);
            oLayer.CreateField(oField, 1);
        }
        //下面填充图形数据和属性值
        FeatureDefn oDefn = oLayer.GetLayerDefn();
        for (PhotoSurvey photoSurvey : dbPhotoSurveyList) {
            Feature oFeature = new Feature(oDefn);
            if (photoSurveyToOGRFeature(photoSurvey, oFeature)) {
                oLayer.CreateFeature(oFeature);
            }
        }
        oDS.SyncToDisk();
        return true;
    }

    private MapScene getCurrentScene() {
        MapScene mapScene = MapApplication.instance().getLayersManager().getCurrentScene();
        return mapScene;
    }

    /**
     * 在地图显示拍摄照片图标
     *
     * @param photoData
     */
    public void showPhotoMark(PhotoSurvey photoData) {
        if (layersManager.getDrawerLayer() == null) {
            //数据尚未准备好
            return;
        }
        String imagePath = photoData.getPhotoImage();
        String thumbnailPath = getThumbnailPath(imagePath);
        Bitmap smallBitmap = loadThumbnail(imagePath, thumbnailPath);
        Resources resources = context.getResources();
        Drawable drawable;
        if (smallBitmap != null) {
            //处理图片为空的情况
            drawable = new BitmapDrawable(MapApplication.getContext().getResources(), smallBitmap);
            //drawable = new RoundImageDrawable(smallBitmap);
        } else
            drawable = resources.getDrawable(R.drawable.image);
        PictureMarkerSymbol markerSymbol = new PictureMarkerSymbol(MapApplication.getContext(), drawable);
        calculateMarkSymbolOffset(markerSymbol, photoData.getAzimuth());
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("comment", photoData.getComment());
        attrs.put("azimuth", photoData.getAzimuth());
        attrs.put("date", photoData.getDate());
        attrs.put("image", photoData.getPhotoImage());
        attrs.put("thumbnail", thumbnailPath);
        attrs.put("longitude", photoData.getLongitude());
        attrs.put("latitude", photoData.getLatitude());
        Point position = new Point(photoData.getLongitude(), photoData.getLatitude(), photoData.getAltitude());
        if (!layersManager.wgs84.equals(layersManager.getMapView().getSpatialReference()))
            position = (Point) GeometryEngine.project(position, layersManager.wgs84, layersManager.getMapView().getSpatialReference());
        Graphic graphic = new Graphic(position, markerSymbol, attrs);
        layersManager.getUserDrawerLayer().addGraphic(graphic);
//		SimpleMarkerSymbol simpleSymbol = new SimpleMarkerSymbol(Color.BLUE,98, SimpleMarkerSymbol.STYLE.CIRCLE);
//		Graphic label = new Graphic(position, simpleSymbol);
//		layersManager.getDrawerLayer().addGraphic(label);
    }

    /**
     * 根据方位角计算图标的位置偏移
     *
     * @param markerSymbol
     * @param azimuth
     */
    private void calculateMarkSymbolOffset(PictureMarkerSymbol markerSymbol, double azimuth) {
        float height = markerSymbol.getHeight() * 0.5f;
        double radian = Math.toRadians(azimuth);
        double offsetX = height * Math.sin(radian);
        double offsetY = height * Math.cos(radian);
        markerSymbol.setOffsetX((float) offsetX);
        markerSymbol.setOffsetY((float) offsetY);
    }

    private String getThumbnailPath(String imagePath) {
        if (imagePath == null)
            return null;
        File file = new File(imagePath);
        if (file.exists()) {
            String fileDir = file.getParent();
            String fileName = file.getName();
            //String prefix = fileName.substring(fileName.lastIndexOf("."));
            String prefix = ".png";
            String fileNameNoExtent = fileName.substring(0, fileName.lastIndexOf(".") - 1);
            String smallFilePath = fileDir + "/" + fileNameNoExtent + "_thumb" + prefix;
            return smallFilePath;
        } else
            return null;
    }

    private Bitmap loadThumbnail(String imagePath, String smallImagePath) {
        Bitmap smallBitmap = null;
        smallBitmap = BitmapFactory.decodeFile(smallImagePath);
        if (smallBitmap != null)
            return smallBitmap;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if (bitmap == null)
            return null;
        //创建点要素资源并且关联图片及图表
        Bitmap tmpSmallBitmap = ThumbnailUtils.extractThumbnail(bitmap, 128, 128);
        smallBitmap = toRoundCorner(tmpSmallBitmap, 20);    //创建倒角
        tmpSmallBitmap.recycle();
        File file = new File(smallImagePath);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
        }
        return smallBitmap;
    }

    private Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    class AsyncLoadPhotoSurvey extends AsyncTask<Void, Integer, Boolean> {
        ProgressBar mProgressBar;

        public AsyncLoadPhotoSurvey(ProgressBar progressBar) {
            super();
            mProgressBar = progressBar;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mProgressBar != null)
                mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.INVISIBLE);
                mProgressBar.setProgress(0);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (mProgressBar != null)
                mProgressBar.setProgress(values[0]);
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            List<PhotoSurvey> dbPhotoSurveyList = DataSupport.findAll(PhotoSurvey.class);
            int size = dbPhotoSurveyList.size();
            int progressInt = 0;
            double sizePercent = size != 0 ? (100.0 / (double) size) : 0;
            for (PhotoSurvey photoSurvey : dbPhotoSurveyList) {
                progressInt++;
                if (isCancelled())
                    return false;
                if (!photoSurveyMap.containsKey(photoSurvey.getId())) {
                    photoSurveyMap.put(photoSurvey.getId(), photoSurvey);
                    showPhotoMark(photoSurvey);
                }
                publishProgress((int) Math.floor(sizePercent * progressInt));
            }
            return true;
        }
    }
}
