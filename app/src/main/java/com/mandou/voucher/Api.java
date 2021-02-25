package com.mandou.voucher;

import okhttp3.OkHttpClient;

/**
 * Created by calvin on 2019/3/3.
 */

public class Api {

    private static final OkHttpClient CLIENT = new OkHttpClient();

    private static final String BASE_URL = "http://cashier.51mandou.com";
//    private static final String BASE_URL = "http://192.168.1.17:8080/cashier-web";
//    private static final String BASE_URL = "https://api.appinchinaservices.com";

    public static final String GET_PAY_TOOLS = "/payTools.json";

    public static final String CREATE_ORDER = "/order.json";

    public static final String QUERY_ORDER = "/detail.json";

    public static final String AGREEMENT_SIGN = "/agreementSign.json";

    public static final String SMS = "/sms.json";

    public static final String LOGIN = "/auth.json";

    public static final String CHECK_LOGIN = "/checkAuth.json";

    public static final String EXPIRE_TIME = "/customerServiceMgmt/%s.json";

    public static final String SESSION = "/report/session.json";

    public static final String ACTION = "/report/action.json";

    public static OkHttpClient getClient() {
        return CLIENT;
    }

    public static String buildUrl(String apiName) {
        return BASE_URL + apiName;
    }
}
