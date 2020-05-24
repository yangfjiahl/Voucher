/**
 * Copyright (C) 2019 ~ 2020 itech.com. All Rights Reserved.
 */
package com.itech.acs.sdk;

/**
 * @author: YangFeng(calvin)
 * @date: 2020/5/24 20:26
 * @description: 后端接口
 * @version: v1.0
 */
public interface Api {

    /**
     * 服务器地址
     */
    String SERVER_URL = "http://cashier.51mandou.com";

    /**
     * session创建
     */
    String SESSION = "/report/session.json";

    /**
     * 事件创建
     */
    String ACTION = "/report/action.json";
}