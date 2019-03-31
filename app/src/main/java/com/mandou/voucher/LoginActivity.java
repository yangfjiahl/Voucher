package com.mandou.voucher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by calvin on 2019/3/30.
 */

public class LoginActivity extends Activity {

    private static String TAG;

    private static final String TOKEN = "TOKEN";

    EditText mobileNo;
    EditText smsCode;
    Button bSmsCode;
    Button bSubmit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        TAG = getClass().getSimpleName();

        init();
    }

    private void init() {
        mobileNo = findViewById(R.id.mobileNo);
        smsCode = findViewById(R.id.smsCode);
        bSmsCode = findViewById(R.id.btn_smsCode);
        bSubmit = findViewById(R.id.btn_submit);

        bSmsCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile = mobileNo.getText().toString();
                if (mobile == null || mobile.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please input mobileNo", Toast.LENGTH_LONG).show();
                    return;
                }

                bSmsCode.setEnabled(false);

                HttpUrl.Builder urlBuilder = HttpUrl.parse(Api.buildUrl(Api.SMS))
                        .newBuilder();
                urlBuilder.addQueryParameter("mobileNo", mobile);

                Request request = new Request.Builder()
                        .url(urlBuilder.build())
                        .headers(PayToolInfo.headers)
                        .build();
                Call call = Api.getClient().newCall(request);
                call.enqueue(new Callback() {

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Looper.prepare();
                        Toast.makeText(LoginActivity.this, "请求服务端失败", Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String s = response.body().string();

                        Log.d(TAG, "onResponse: " + s);

                        Looper.prepare();
                        Toast.makeText(LoginActivity.this, "Please check sms authcode", Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                });
            }
        });

        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile = mobileNo.getText().toString();
                if (mobile == null || mobile.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please input mobileNo", Toast.LENGTH_LONG).show();
                    return;
                }

                String authCode = smsCode.getText().toString();
                if (authCode == null || authCode.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please input authCode", Toast.LENGTH_LONG).show();
                    return;
                }

                FormBody.Builder builder = new FormBody.Builder();
                builder.add("mobileNo", mobile);
                builder.add("authCode", authCode);

                Request request = new Request.Builder()
                        .url(Api.buildUrl(Api.LOGIN))
                        .post(builder.build())
                        .headers(PayToolInfo.headers)
                        .build();
                Call call = Api.getClient().newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Looper.prepare();
                        Toast.makeText(LoginActivity.this, "请求服务端失败", Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String s = response.body().string();

                        Log.d(TAG, "onResponse: " + s);

                        JSONObject result = JSON.parseObject(s);

                        String code = result.getString("code");

                        if ("0".equals(code)) {
                            JSONObject data = result.getJSONObject("data");
                            PreferenceHelper.setValue(TOKEN, data.toJSONString());
                            finish();
                        } else {
                            Looper.prepare();
                            Toast.makeText(LoginActivity.this, result.getString("msg"), Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }
                    }
                });
            }
        });
    }
}
