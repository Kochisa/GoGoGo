package com.zcshou.utils;

/**
 * 地图坐标转换工具类
 * 提供各种坐标系统之间的转换功能：
 * 1. 百度坐标(BD09)转WGS84坐标
 * 2. WGS84坐标转百度坐标(BD09)
 * 3. 百度坐标(BD09)转GCJ02坐标
 * 4. GCJ02坐标转WGS84坐标
 */
public class MapUtils {
    // 数学常量
    public final static double pi = 3.14159265358979324; // π
    public final static double a = 6378245.0; // 长半轴
    public final static double ee = 0.00669342162296594323; // 偏心率平方
    public final static double x_pi = 3.14159265358979324 * 3000.0 / 180.0; // 3π/180
    /**
     * 百度坐标(BD09)转WGS84坐标
     * @param lon 百度经度
     * @param lat 百度纬度
     * @return WGS84坐标数组 [经度, 纬度]
     */
    public static double[] bd2wgs(double lon, double lat) {
        double[] bd2Gcj = bd09togcj02(lon, lat);
        return gcj02towgs84(bd2Gcj[0], bd2Gcj[1]);
    }
    /**
     * WGS84坐标转百度坐标(BD09)
     * @param lng WGS84经度
     * @param lat WGS84纬度
     * @return 百度坐标数组 [经度, 纬度]
     */
    public static double[] wgs2bd09(double lng, double lat){
        double dlat = transformLat(lng - 105.0, lat - 35.0);
        double dlng = transformLon(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 * pi;
        double magic = Math.sin(radlat);
        magic = 1 - ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * pi);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * pi);
        double mglat = lat + dlat;
        double mglng = lng + dlng;
        double z = Math.sqrt(mglng * mglng + mglat * mglat) + 0.00002 * Math.sin(mglat * x_pi);
        double theta = Math.atan2(mglat, mglng) + 0.000003 * Math.cos(mglng * x_pi);
        double bd_lng = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new double[] { bd_lng, bd_lat };
    }
    /**
     * 百度坐标(BD09)转GCJ02坐标
     * @param bd_lon 百度经度
     * @param bd_lat 百度纬度
     * @return GCJ02坐标数组 [经度, 纬度]
     */
    public static double[] bd09togcj02(double bd_lon, double bd_lat) {
        double x = bd_lon - 0.0065;
        double y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double gg_lng = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new double[] { gg_lng, gg_lat };
    }
    /**
     * GCJ02坐标转WGS84坐标
     * @param lng GCJ02经度
     * @param lat GCJ02纬度
     * @return WGS84坐标数组 [经度, 纬度]
     */
    public static double[] gcj02towgs84(double lng, double lat) {
        double dlat = transformLat(lng - 105.0, lat - 35.0);
        double dlng = transformLon(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 * pi;
        double magic = Math.sin(radlat);
        magic = 1 - ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * pi);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * pi);
        double mglat = lat + dlat;
        double mglng = lng + dlng;
        return new double[] { lng * 2 - mglng, lat * 2 - mglat };
    }
    /**
     * 转换纬度
     * @param lat 纬度
     * @param lon 经度
     * @return 转换后的纬度
     */
    private static double transformLat(double lat, double lon) {
        double ret = -100.0 + 2.0 * lat + 3.0 * lon + 0.2 * lon * lon + 0.1 * lat * lon + 0.2 * Math.sqrt(Math.abs(lat));
        ret += (20.0 * Math.sin(6.0 * lat * pi) + 20.0 * Math.sin(2.0 * lat * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lon * pi) + 40.0 * Math.sin(lon / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lon / 12.0 * pi) + 320 * Math.sin(lon * pi  / 30.0)) * 2.0 / 3.0;
        return ret;
    }
    /**
     * 转换经度
     * @param lat 纬度
     * @param lon 经度
     * @return 转换后的经度
     */
    private static double transformLon(double lat, double lon) {
        double ret = 300.0 + lat + 2.0 * lon + 0.1 * lat * lat + 0.1 * lat * lon + 0.1 * Math.sqrt(Math.abs(lat));
        ret += (20.0 * Math.sin(6.0 * lat * pi) + 20.0 * Math.sin(2.0 * lat * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * pi) + 40.0 * Math.sin(lat / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lat / 12.0 * pi) + 300.0 * Math.sin(lat / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }
}