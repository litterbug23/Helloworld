package com.example.administrator.mapdev.tools;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;

public class GeometryUtils {

    /**
     * POINT(6 10)
     * LINESTRING(3 4,10 50,20 25)
     * POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2))
     * MULTIPOINT(3.5 5.6, 4.8 10.5)
     * MULTILINESTRING((3 4,10 50,20 25),(-5 -8,-10 -8,-15 -4))
     * MULTIPOLYGON(((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2)),((6 3,9 2,9 4,6 3)))
     * 将几何对象生成wkt字符串
     */
    public static String GeometryToWKT(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        String wktString;
        Geometry.Type type = geometry.getType();
        if (type == Geometry.Type.POINT) {
            Point pt = (Point) geometry;
            wktString = "POINT(" + pt.getX() + " " + pt.getY() + ")";
        } else if (type == Geometry.Type.POLYGON || type == Geometry.Type.POLYLINE) {
            MultiPath multiPath = (MultiPath) geometry;
            int pathSize = multiPath.getPathCount();
            if (type == Geometry.Type.POLYLINE) {
                if (pathSize == 1)
                    wktString = "LINESTRING";
                else
                    wktString = "MULTILINESTRING(";
            } else {
                if (pathSize == 1)
                    wktString = "POLYGON(";
                else
                    wktString = "MULTIPOLYGON(";
            }
            for (int j = 0; j < pathSize; j++) {
                String temp = "(";
                int size = multiPath.getPathSize(j);
                for (int i = 0; i < size; i++) {
                    Point pt = multiPath.getPoint(i);
                    temp += pt.getX() + " " + pt.getY() + ",";
                }
                temp = temp.substring(0, temp.length() - 1) + ")";
                wktString += temp + ",";
            }
            wktString = wktString.substring(0, wktString.length() - 1) + ")";
        } else if (type == Geometry.Type.ENVELOPE) {
            Envelope env = (Envelope) geometry;
            wktString = "POLYGON ((" +
                    env.getXMin() + " " + env.getYMin() + "," +
                    env.getXMax() + " " + env.getYMin() + "," +
                    env.getXMax() + " " + env.getYMax() + "," +
                    env.getXMin() + " " + env.getYMax() + "," +
                    env.getXMin() + " " + env.getYMin() + "))";
        } else if (type == Geometry.Type.MULTIPOINT) {
            MultiPoint multiPoint = (MultiPoint) geometry;
            // MULTIPOINT(3.5 5.6, 4.8 10.5)
            wktString = "MULTIPOINT(";
            int size = multiPoint.getPointCount();
            for (int i = 0; i < size - 1; i++) {
                wktString += multiPoint.getPoint(i).getX();
                wktString += " ";
                wktString += multiPoint.getPoint(i).getY();
                wktString += ",";
            }
            wktString += multiPoint.getPoint(size - 1).getX();
            wktString += " ";
            wktString += multiPoint.getPoint(size - 1).getY();
            wktString += ")";
        } else {
            wktString = null;
        }
        return wktString;
    }


    /**
     * 将wkt字符串拼成几何对象
     */
    public static Geometry WKTToGeometry(String wkt) {
        Geometry geo = null;
        if (wkt == null || wkt.equals("")) {
            return null;
        }
        String headStr = wkt.substring(0, wkt.indexOf("("));
        String temp = wkt.substring(wkt.indexOf("(") + 1, wkt.lastIndexOf(")"));
        if (headStr.equalsIgnoreCase("Point")) {
            String[] values = temp.split(" ");
            geo = new Point(Double.valueOf(values[0]), Double.valueOf(values[1]));
        } else if (headStr.equalsIgnoreCase("Polyline") || headStr.equalsIgnoreCase("Polygon")) {
            geo = parseWKT(temp, headStr);
        } else if (headStr.equalsIgnoreCase("Envelope")) {
            String[] extents = temp.split(",");
            geo = new Envelope(Double.valueOf(extents[0]), Double.valueOf(extents[1]), Double.valueOf(extents[2]), Double.valueOf(extents[3]));
        } else if (headStr.equalsIgnoreCase("MultiPoint")) {

        } else {
            return null;
        }
        return geo;
    }

    private static Geometry parseWKT(String multipath, String type) {
        String subMultipath = multipath.substring(1, multipath.length() - 1);
        String[] paths;
        if (subMultipath.indexOf("),(") >= 0) {
            paths = subMultipath.split("),(");//多个几何对象的字符串
        } else {
            paths = new String[]{subMultipath};
        }
        Point startPoint = null;
        MultiPath path = null;
        if (type.equals("Polyline")) {
            path = new Polyline();
        } else {
            path = new Polygon();
        }
        for (int i = 0; i < paths.length; i++) {
            String[] points = paths[i].split(",");
            startPoint = null;
            for (int j = 0; j < points.length; j++) {
                String[] pointStr = points[j].split(" ");
                if (startPoint == null) {
                    startPoint = new Point(Double.valueOf(pointStr[0]), Double.valueOf(pointStr[1]));
                    path.startPath(startPoint);
                } else {
                    path.lineTo(new Point(Double.valueOf(pointStr[0]), Double.valueOf(pointStr[1])));
                }
            }
        }
        return path;
    }
}
