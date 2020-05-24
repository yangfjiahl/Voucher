/**
 * Copyright (C) 2019 ~ 2020 itech.com. All Rights Reserved.
 */
package com.itech.acs.sdk;

import android.app.Application;
import android.content.Context;

/**
 * @author: YangFeng(calvin)
 * @date: 2020/5/24 19:32
 * @description: 客户端配置
 * @version: v1.0
 */
public class AcsClientConfig {

    protected Context context = null;

    protected String appKey = null;

    protected String appSecret = null;

    protected boolean loggingEnabled = false;

    protected Application application = null;

    public AcsClientConfig(String appKey, String appSecret) {
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    public void setApplication(Application application) {
        this.application = application;
        this.context = application;
    }
}