package com.mandou.voucher;

import android.app.Application;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mandou.voucher.util.NetworkUtil;
import com.mandou.voucher.util.SystemUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by calvin on 2019/3/30.
 */

public class VoucherApp extends Application {

    private static String TAG;

    private static final String TOKEN = "TOKEN";

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = getClass().getSimpleName();

        PreferenceHelper.init(this);
        SessionHelper.init(this);

        // API: check whether token is expired or not
        checkLogin();
    }

    private void checkLogin() {
        String tokenStr = PreferenceHelper.getValue(TOKEN);
        if (tokenStr != null && !tokenStr.isEmpty()) {
            JSONObject json = JSONObject.parseObject(tokenStr);
            String token = json.getString("token");

            HttpUrl.Builder urlBuilder = HttpUrl.parse(Api.buildUrl(Api.CHECK_LOGIN))
                    .newBuilder();
            urlBuilder.addQueryParameter("token", token);

            Request request = new Request.Builder()
                    .url(urlBuilder.build())
                    .headers(PayToolInfo.headers)
                    .build();
            Call call = Api.getClient().newCall(request);
            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String s = response.body().string();

                    Log.d(TAG, "onResponse: " + s);

                    JSONObject result = JSON.parseObject(s);

                    if (!result.getBoolean("data")) {
                        // token expired, delete it
                        PreferenceHelper.remove(TOKEN);
                    }  else {
                        SessionHelper.startSession();
                    }
                }
            });
        } else {
            Log.d(TAG, "No credential");
        }
    }
}
