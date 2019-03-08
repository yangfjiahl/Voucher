package com.mandou.voucher;

import okhttp3.Headers;

/**
 * Created by calvin on 2019/3/3.
 */

public class PayToolInfo {

    private static final String APP_ID = "20190307001";
    private static final String APP_SECRET = "123";
    public static final Headers headers;

    static {
        headers = Headers.of("APP_ID", APP_ID, "APP_SECRET", APP_SECRET);
    }

    private static String WECHAT_APP_ID;

    private static String CURRENT_BIZ_NO;

    public static void setWechatAppId(String wechatAppId) {
        WECHAT_APP_ID = wechatAppId;
    }

    public static String getWechatAppId() {
        return WECHAT_APP_ID;
    }

    public static void setCurrentBizNo(String currentBizNo) {
        CURRENT_BIZ_NO = currentBizNo;
    }

    public static String getCurrentBizNo() {
        return CURRENT_BIZ_NO;
    }
}
