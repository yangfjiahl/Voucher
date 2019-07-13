package com.mandou.voucher;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mandou.voucher.util.NetworkUtil;
import com.mandou.voucher.util.SystemUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 用户session
 * created by Dell on 2019/7/13
 */
public class Session {

    private static String TAG = "Session";

    private static final String TOKEN = "TOKEN";

    /**
     * 保存session
     * @param context
     */
    public void saveSession(Context context){

        Map<String,Object> params = new HashMap<>();

        String tokenStr = PreferenceHelper.getValue(TOKEN);
        if (tokenStr != null && !tokenStr.isEmpty()) {
            params.put("customerIdentity", JSONObject.parseObject(tokenStr).getString("customerId"));
        }

        params.put("deviceType", SystemUtil.getDeviceModel());
        params.put("deviceFactory",SystemUtil.getDeviceBrand());
        params.put("osVersion",SystemUtil.getSysVersion());
        params.put("deviceId",SystemUtil.getDeviceId(context));
        params.put("ipAddress",NetworkUtil.getIp(context));
        params.put("networkType",NetworkUtil.getNetworkType(context));

        double[] location = SystemUtil.getLongitude(context);
        if(location.length==2){
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

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().toString();
                JSONObject result = JSON.parseObject(s);

            }
        });

    }

}
