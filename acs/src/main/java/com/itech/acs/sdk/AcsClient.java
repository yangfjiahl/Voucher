/**
 * Copyright (C) 2019 ~ 2020 itech.com. All Rights Reserved.
 */
package com.itech.acs.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: YangFeng(calvin)
 * @date: 2020/5/24 19:30
 * @description: 统计客户端
 * @version: v1.0
 */
public class AcsClient {

    public static final String TAG = "AcsClient";

    /**
     * 配置
     */
    private AcsClientConfig acsClientConfig;

    private static final String PAY_PREFIX = "PAY:";

    /**
     * 客户端初始化
     *
     * @param application
     * @param config
     * @return
     */
    public synchronized AcsClient init(Application application, AcsClientConfig config) {
        if (application == null) {
            throw new IllegalArgumentException("valid application is required in init, but was provided 'null'");
        }

        if (config.appKey == null || config.appKey.length() == 0) {
            throw new IllegalArgumentException("valid appKey is required, but was provided either 'null' or empty String");
        }

        if (config.appSecret == null || config.appSecret.length() == 0) {
            throw new IllegalArgumentException("valid appSecret is required, but was provided either 'null' or empty String");
        }

        this.acsClientConfig = config;
        this.acsClientConfig.setApplication(application);
        this.acsClientConfig.application.registerActivityLifecycleCallbacks(new AcsActivityLifecycleCallbacks());

        PreferenceHelper.init(application);

        sendSessionEvent();
        return this;
    }

    /**
     * 设置用户标识
     */
    public void setCustomerIdentity(String customerIdentity) {
        PreferenceHelper.setValue(Consts.CUSTOMER_IDENTITY, customerIdentity);
    }

    /**
     * session创建事件
     */
    private void sendSessionEvent() {
        Map<String, Object> params = new HashMap<>();

        Context context = AcsClient.sharedInstance().getAcsClientConfig().context;

        params.put("deviceType", SystemUtil.getDeviceType());
        params.put("deviceFactory", SystemUtil.getDeviceBrand());
        params.put("osVersion", SystemUtil.getSysVersion());

        params.put("appStore", SystemUtil.getAppStore(context));
        params.put("appVersion", SystemUtil.getVersionName(context));
        params.put("networkType", NetworkUtil.getNetworkType(context));

        double[] location = SystemUtil.getLongitude(context);
        if (location.length == 2) {
            params.put("longitude", location[0]);
            params.put("latitude", location[1]);
        }

        params.put(Consts.API, Api.SESSION);

        try {
            EventUtil.emit(params);
        } catch (Exception e) {

        }
    }

    /**
     * 自定义事件
     *
     * @param activity
     * @param eventName
     * @param extra
     */
    public void reportEvent(Activity activity, String eventName, Map<String, Object> extra) {
        Map<String, Object> params = buildCommonParams(activity, eventName);
        params.put("isPaymentPage", false);

        EventUtil.emit(params);
    }

    /**
     * 支付事件
     *
     * @param activity
     * @param eventName
     * @param extra
     */
    public void reportEventWithPayment(Activity activity, String eventName, Map<String, Object> extra) {
        Map<String, Object> params = buildCommonParams(activity, eventName);

        params.put("isPaymentPage", true);
        params.put("pageName", PAY_PREFIX + params.get("pageName"));
        EventUtil.emit(params);
    }

    @NonNull
    private Map<String, Object> buildCommonParams(Activity context, String eventName) {
        Map<String, Object> params = new HashMap<>();
        params.put("actionType", "TAP");
        params.put("pageName", context.getLocalClassName());
        params.put("eventName", eventName);
        params.put("gmtOccur", new Date().getTime());

        params.put(Consts.API, Api.ACTION);
        return params;
    }

    /**
     * 开启日志
     */
    public void setLoggingEnabled() {
        acsClientConfig.loggingEnabled = true;
    }

    public boolean isLoggingEnabled() {
        return acsClientConfig.loggingEnabled;
    }

    private static class SingletonHolder {
        static final AcsClient instance = new AcsClient();
    }

    protected AcsClientConfig getAcsClientConfig() {
        return acsClientConfig;
    }

    /**
     * 单例
     */
    public static AcsClient sharedInstance() {
        return AcsClient.SingletonHolder.instance;
    }
}