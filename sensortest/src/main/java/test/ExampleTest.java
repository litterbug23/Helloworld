package test;

import android.test.InstrumentationTestCase;

import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;

/**
 * Created by Administrator on 2016/6/5.
 */
public class ExampleTest extends InstrumentationTestCase {

    public void test() throws Exception {
        final int expected = 1;
        final int reality = 1;
        assertEquals(expected, reality);
    }

    static void WriteVectorFile()
    {
        String strVectorFile ="E:\\TestPolygon.shp";
        // 注册所有的驱动
        ogr.RegisterAll();

        // 为了支持中文路径，请添加下面这句代码
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "NO");
        // 为了使属性表字段支持中文，请添加下面这句
        gdal.SetConfigOption("SHAPE_ENCODING","");
        //创建数据，这里以创建ESRI的shp文件为例
        String strDriverName = "ESRI Shapefile";
        org.gdal.ogr.Driver oDriver =ogr.GetDriverByName(strDriverName);
        if (oDriver == null)
        {
            System.out.println(strVectorFile+ " 驱动不可用！\n");
            return;
        }
        // 创建数据源
        DataSource oDS = oDriver.CreateDataSource(strVectorFile,null);
        if (oDS == null)
        {
            System.out.println("创建矢量文件【"+ strVectorFile +"】失败！\n" );
            return;
        }
        // 创建图层，创建一个多边形图层，这里没有指定空间参考，如果需要的话，需要在这里进行指定
        Layer oLayer =oDS.CreateLayer("TestPolygon", null, ogr.wkbPolygon, null);
        if (oLayer == null)
        {
            System.out.println("图层创建失败！\n");
            return;
        }
        // 下面创建属性表
        // 先创建一个叫FieldID的整型属性
        FieldDefn oFieldID = new FieldDefn("FieldID", ogr.OFTInteger);
        oLayer.CreateField(oFieldID, 1);
        // 再创建一个叫FeatureName的字符型属性，字符长度为50
        FieldDefn oFieldName = new FieldDefn("FieldName", ogr.OFTString);
        oFieldName.SetWidth(100);
        oLayer.CreateField(oFieldName, 1);
        FeatureDefn oDefn =oLayer.GetLayerDefn();
        // 创建三角形要素
        Feature oFeatureTriangle = new Feature(oDefn);
        oFeatureTriangle.SetField(0, 0);
        oFeatureTriangle.SetField(1, "三角形");
        Geometry geomTriangle = Geometry.CreateFromWkt("POLYGON ((0 0,20 0,10 15,0 0))");
        oFeatureTriangle.SetGeometry(geomTriangle);
        oLayer.CreateFeature(oFeatureTriangle);
        // 创建矩形要素
        Feature oFeatureRectangle = new Feature(oDefn);
        oFeatureRectangle.SetField(0, 1);
        oFeatureRectangle.SetField(1, "矩形");
        Geometry geomRectangle =Geometry.CreateFromWkt("POLYGON ((30 0,60 0,60 30,30 30,30 0))");
        oFeatureRectangle.SetGeometry(geomRectangle);
        oLayer.CreateFeature(oFeatureRectangle);
        // 创建五角形要素
        Feature oFeaturePentagon = new Feature(oDefn);
        oFeaturePentagon.SetField(0, 2);
        oFeaturePentagon.SetField(1, "五角形");
        Geometry geomPentagon =Geometry.CreateFromWkt("POLYGON ((70 0,85 0,90 15,80 30,65 15,70 0))");
        oFeaturePentagon.SetGeometry(geomPentagon);
        oLayer.CreateFeature(oFeaturePentagon);
        System.out.println("\n数据集创建完成！\n");
    }
}
