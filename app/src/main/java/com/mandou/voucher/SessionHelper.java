package com.mandou.voucher;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.mandou.voucher.util.NetworkUtil;
import com.mandou.voucher.util.SystemUtil;
import com.mandou.voucher.wxapi.ActionHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SessionHelper {
    private static String TAG = "Session";
    private static final String TOKEN = "TOKEN";

    private static Context context;

    public static void init(Context context) {
        SessionHelper.context = context;
        ActionHelper.init(context);
    }

    public static void startSession() {
        Map<String, Object> params = new HashMap<>();

        String tokenStr = PreferenceHelper.getValue(TOKEN);
        params.put("customerIdentity", JSONObject.parseObject(tokenStr).getString("customerId"));

        params.put("deviceType", SystemUtil.getDeviceType());
        params.put("deviceFactory", SystemUtil.getDeviceBrand());
        params.put("osVersion", SystemUtil.getSysVersion());
        params.put("deviceId", SystemUtil.getDeviceId(context));
        params.put("appStore", SystemUtil.getAppStore(context));
        params.put("appVersion", SystemUtil.getVersionName(context));
        params.put("networkType", NetworkUtil.getNetworkType(context));

        double[] location = SystemUtil.getLongitude(context);
        if (location.length == 2) {
            params.put("longitude", location[0]);
            params.put("latitude", location[1]);
        }

        Request request = new Request.Builder()
                .url(Api.buildUrl(Api.SESSION))
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
                Log.d(TAG, "onResponse: " + s);
            }
        });
    }

}
