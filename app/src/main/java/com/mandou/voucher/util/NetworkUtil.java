package com.mandou.voucher.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

/**
 * 网络状态工具类
 * created by Dell on 2019/7/13
 */
public class NetworkUtil {

    /**
     * 网络是否连接
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {

            ConnectivityManager mConnectivityManager = getConnManager(context);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 当前网络连接类型
     *
     * <p>wifi | 4G|3G|2G</p>
     *
     * @param context
     * @return
     */
    public static String getNetworkType(Context context) {

        String netType = "NONE";

        if (context != null) {
            return netType;
        }

        ConnectivityManager mConnectivityManager = getConnManager(context);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

        if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
            return netType;
        }

        int nType = mNetworkInfo.getType();

        if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = "WIFI";

        } else if (nType == ConnectivityManager.TYPE_MOBILE) {

            int nSubType = mNetworkInfo.getSubtype();

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (nSubType == TelephonyManager.NETWORK_TYPE_LTE && !telephonyManager.isNetworkRoaming()) {
                netType = "4G";

            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                    || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    && !telephonyManager.isNetworkRoaming()) {
                //3G网络   联通的3G为UMTS或HSDPA 电信的3G为EVDO
                netType = "3G";

            } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
                    || nSubType == TelephonyManager.NETWORK_TYPE_EDGE
                    || nSubType == TelephonyManager.NETWORK_TYPE_CDMA
                    && !telephonyManager.isNetworkRoaming()) {

                // 2G网络 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA
                netType = "2G";

            } else

                netType = "NO DISPLAY";
        }

        return netType;
    }

    /**
     * 获取IP地址
     *
     * @param context
     * @return
     */
    public static String getIp(Context context){
        //获取wifi服务
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            //判断wifi是否开启
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return int2IP(wifiInfo.getIpAddress());

        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * get connectivyManager
     * @param context
     * @return
     */
    private static ConnectivityManager getConnManager(Context context){
        if(context != null){
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if(mConnectivityManager != null){
                return mConnectivityManager;
            }
        }
        return  null;
    }

    /**
     * ip 转换
     * @param ipInt
     * @return
     */
    private static String int2IP(int ipInt){
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }
}
