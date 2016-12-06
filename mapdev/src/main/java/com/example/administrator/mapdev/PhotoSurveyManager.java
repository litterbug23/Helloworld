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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;

import org.litepal.LitePalApplication;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private GraphicsLayer userDrawerLayer;
    private LayersManager layersManager;
    private Context context = LitePalApplication.getContext();
    private GpsLocationService gpsLocationService;
    private SensorService sensorService;
    private Map<Long, PhotoSurvey> photoSurveyMap = new HashMap<>();

    //private List<PhotoSurvey> photoSurveyList=null;
    public PhotoSurveyManager(LayersManager layersManager, GpsLocationService gpsLocationService, SensorService sensorService) {
        initPhotoSurveyDrawer(layersManager, gpsLocationService, sensorService);
    }

    public void initPhotoSurveyDrawer(LayersManager layersManager, GpsLocationService gpsLocationService, SensorService sensorService) {
        this.layersManager = layersManager;
        this.gpsLocationService = gpsLocationService;
        this.sensorService = sensorService;
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
     * @param imagePath
     * @param location
     * @param comment
     */
    public void takePhotoAction(String imagePath,Location location,String comment) {
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
        photoSurvey.setDate(now.getTime());
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
        takePhotoAction(imagePath,location,comment);
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
        Drawable drawable = null;
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
