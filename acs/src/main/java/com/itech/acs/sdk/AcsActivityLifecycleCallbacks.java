/**
 * Copyright (C) 2019 ~ 2020 itech.com. All Rights Reserved.
 */
package com.itech.acs.sdk;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: YangFeng(calvin)
 * @date: 2020/5/24 19:59
 * @description: 生命周期方法
 * @version: v1.0
 */
public class AcsActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    /**
     * 输出日志
     *
     * @param s
     */
    private void log(String s) {
        if (AcsClient.sharedInstance().isLoggingEnabled()) {
            Log.d(AcsClient.TAG, "onActivityCreated," + s);
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        log(activity.getLocalClassName());
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        log(activity.getLocalClassName());
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        log(activity.getLocalClassName());

        sendNavigateEvent(activity.getLocalClassName());
    }

    /**
     * 页面跳转上报
     *
     * @param pageName
     */
    private void sendNavigateEvent(String pageName) {
        Map<String, Object> params = new HashMap<>();
        params.put("actionType", "NAVIGATE");
        params.put("pageName", pageName);
        params.put("gmtOccur", new Date().getTime());
        params.put(Consts.API, Api.ACTION);
        EventUtil.emit(params);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        log(activity.getLocalClassName());
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        log(activity.getLocalClassName());
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        log(activity.getLocalClassName());
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        log(activity.getLocalClassName());
    }
}