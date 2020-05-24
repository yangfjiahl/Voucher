package com.itech.acs.sdk;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 系统工具类
 * <p>
 * created by Dell on 2019/7/13
 */
public class SystemUtil {

    private static String tag = SystemUtil.class.getSimpleName();

    /**
     * 获取手机厂商名称
     *
     * @return
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    /**
     * 获取手机型号(设备类型 )
     *
     * @return
     */
    public static String getDeviceType() {
        return "ANDROID";
    }

    /**
     * 获取当前系统版本号
     *
     * @return
     */
    public static String getSysVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     *
     * @return
     */
    public static String getDeviceId(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
            if (tm != null) {

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // TODO:

                }

                String deviceId = tm.getDeviceId();
                return deviceId == null ? "unknown" : deviceId;
            }
        } catch (Exception e) {

        }
        return "unknown";
    }

    /**
     * 获取经纬度
     * <p>返回结果:第一个值为经度，第二个值为纬度</p>
     *
     * @param context
     * @return Array
     */
    public static double[] getLongitude(Context context) {
        double longitude = 0.0;
        double latitude = 0.0;

        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            // 通GPS获取 经纬度
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                }

                try {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                } catch (Exception e) {

                }

            } else {

                LocationListener locationListener = new LocationListener() {

                    // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    // Provider被enable时触发此函数，比如GPS被打开
                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    // Provider被disable时触发此函数，比如GPS被关闭
                    @Override
                    public void onProviderDisabled(String provider) {
                    }

                    //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            Log.e(tag, "Location changed : Lat: " + location.getLatitude()
                                    + " Lng: " + location.getLongitude());
                        }
                    }
                };

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude(); //经度
                    longitude = location.getLongitude(); //纬度
                }

            }
        } catch (Exception e) {

        }

        return new double[]{longitude, latitude};
    }

    public static String getAppStore(Context context) {
        String value = "";
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            value = appInfo.metaData.getString("UMENG_CHANNEL");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String getVersionName(Context context) {
        //获取包信息
        try {
            //获取包管理器
            PackageManager pm = context.getPackageManager();

            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            //返回版本号
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;

    }
}
