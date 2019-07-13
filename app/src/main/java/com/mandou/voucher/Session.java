package com.mandou.voucher;

import android.os.Build;

/**
 * 用户session
 * created by Dell on 2019/7/13
 */
public class Session {


    /**
     * 获取手机厂商名称
     * @return
     */
    public String getDeviceBrand(){
        return Build.BRAND;
    }

    /**
     * 获取手机型号
     * @return
     */
    public String getDeviceModel(){
        return Build.MODEL;
    }

    /**
     * 获取当前系统版本号
     * @return
     */
    public String getSysVersion(){
        return Build.VERSION.RELEASE;
    }

}
