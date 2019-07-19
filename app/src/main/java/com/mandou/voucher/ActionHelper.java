package com.mandou.voucher;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.mandou.voucher.util.SystemUtil;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * actionHelper
 * created by Dell on 2019/7/19
 */
public class ActionHelper {

    private static final String TAG = "ActionHelper";

    private static final String TOKEN = "TOKEN";

    /*支付页面名称前缀**/
    private static final String  PAY_PREFIX = "PAY";

    /* 支付页面跳转类型*/
    public static final String ACTION_TYPE_NAVIGATE = "01";

    /*支付页面点击类型*/
    public static final String ACTION_TYPE_TAP = "11";

    private static Context context;

    public static void init(Context context) {
        ActionHelper.context = context;
    }

    /**
     * 页面跳转及点击上报统计
     *
     * @param actionType
     * @param pageName
     * @param eventName
     */
    public static void startAction(String actionType,String pageName,String eventName){

        Map<String ,Object> params = new HashMap<>();
        String tokenStr = PreferenceHelper.getValue(TOKEN);

        params.put("customerIdentity", JSONObject.parseObject(tokenStr).getString("customerId"));
        params.put("deviceId", SystemUtil.getDeviceId(context));
        params.put("actionType",actionType);
        params.put("pageName",getPageName(pageName));
        params.put("eventName",eventName);
        params.put("attachData",tokenStr);
        params.put("gmtOccur",new Date());

        Request request = new Request.Builder()
                .url(Api.buildUrl(Api.ACTION))
                .post(RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), JSONObject.toJSONString(params)))
                .headers(PayToolInfo.headers)
                .build();

        Call call = Api.getClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(context, "请求服务端失败", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().toString();
                Log.d(TAG, "onResponse: 【 " + s + " ");
            }
        });
    }

    /**
     * 获取页面名称
     *
     * TODO: 如果是支付页面以 "PAY" 开头
     *
     * @param pageName
     * @return
     */
    private static String getPageName(String pageName){
        if(pageName.toUpperCase().startsWith(PAY_PREFIX)){
            return PAY_PREFIX+pageName;
        }
        return pageName;
    }


}
