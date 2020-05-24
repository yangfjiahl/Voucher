/**
 * Copyright (C) 2019 ~ 2020 itech.com. All Rights Reserved.
 */
package com.itech.acs.sdk;

import android.content.Context;

import org.json.JSONException;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: YangFeng(calvin)
 * @date: 2020/5/24 20:24
 * @description: 网络工具
 * @version: v1.0
 */
public class EventUtil {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * 发送
     *
     * @param params
     * @throws JSONException
     */
    public static void emit(Map<String, Object> params) {
        String pageName = (String) params.get("pageName");
        int index = pageName.lastIndexOf('.');
        if (index > 0) {
            params.put("pageName", pageName.substring(index + 1));
        }

        ConnectionThread processor = new ConnectionThread();
        processor.createEvent(params);

        executor.execute(processor);
    }
}