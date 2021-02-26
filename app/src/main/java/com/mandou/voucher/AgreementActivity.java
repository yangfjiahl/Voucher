package com.mandou.voucher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AgreementActivity extends BaseActivity {

    private static String TAG;

    private static final String TOKEN = "TOKEN";

    TextView tip;
    Button query_agreement;
    Button sign_agreement;
    Button unsign_agreement;
    Button sdk;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);

        tip = findViewById(R.id.tip);
        query_agreement = findViewById(R.id.query_agreement);
        sign_agreement = findViewById(R.id.sign_agreement);
        unsign_agreement = findViewById(R.id.unsign_agreement);
        sdk = findViewById(R.id.sdk);

        query_agreement.setOnClickListener(v -> queryAgreement());
        sign_agreement.setOnClickListener(v -> signAgreement());
        unsign_agreement.setOnClickListener(v -> unsignAgreement());
        sdk.setOnClickListener(v->startActivity(new Intent(this, AgreementSdkActivity.class)));
    }

    private static final int MSG_QUERY = 1;
    private static final int MSG_SIGN = 2;
    private static final int MSG_UNSIGN = 3;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            tip.setText(msg.getData().getString("data"));
        }
    };

    private void queryAgreement() {
        String tokenStr = checkToken();
        if (tokenStr == null) return;

        String customerIdentity = JSONObject.parseObject(tokenStr).getString("customerId");

        Request request = new Request.Builder().url(Api.buildUrl(Api.AGREEMENT_QUERY) + "?customerIdentity=" + customerIdentity + "&serviceNo=VIP")
                .headers(PayToolInfo.headers).build();

        Log.d(TAG, request.headers().toString());

        Call call = Api.getClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(AgreementActivity.this, "请求服务端失败", Toast.LENGTH_LONG).show();
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

                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("data", "agreementStatus=" + data.getString("agreementStatus") + "\n" +
                            "nextExecDate=" + new Date(data.getLong("lastExecDate")).toLocaleString());
                    msg.setData(bundle);
                    msg.what = MSG_QUERY;
                    handler.sendMessage(msg);
                }
                else {
                    tip.setText(result.getString("msg"));
                }
            }
        });
    }

    private void unsignAgreement() {
        String tokenStr = checkToken();
        if (tokenStr == null) return;

        Map<String, Object> agreementPayParams = new HashMap<>();
        agreementPayParams.put("customerIdentity", JSONObject.parseObject(tokenStr).getString("customerId"));
        agreementPayParams.put("serviceNo", "VIP");

        Request request = new Request.Builder().url(Api.buildUrl(Api.AGREEMENT_UNSIGN))
                .post(RequestBody.create(
                        MediaType.parse("application/json;charset=UTF-8"),
                        JSONObject.toJSONString(agreementPayParams)))
                .headers(PayToolInfo.headers).build();

        Log.d(TAG, request.headers().toString());

        Call call = Api.getClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(AgreementActivity.this, "请求服务端失败", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().string();
                Log.d(TAG, "onResponse: " + s);

                JSONObject result = JSON.parseObject(s);

                String code = result.getString("code");

                if ("0".equals(code)) {
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("data", "unsign success");
                    msg.setData(bundle);
                    msg.what = MSG_UNSIGN;
                    handler.sendMessage(msg);
                }
                else {
                    Looper.prepare();
                    Toast.makeText(AgreementActivity.this, result.getString("msg"),
                            Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        });
    }

    private void signAgreement() {
        String tokenStr = checkToken();
        if (tokenStr == null) return;

        Map<String, Object> agreementPayParams = new HashMap<>();
        agreementPayParams.put("customerIdentity", JSONObject.parseObject(tokenStr).getString("customerId"));
        agreementPayParams.put("scene", "INDUSTRY|MOBILE");
        agreementPayParams.put("payChannel", "ALIPAY");
        agreementPayParams.put("periodType", "DAY");
        agreementPayParams.put("periodValue", 7);
        agreementPayParams.put("nextExecDate", new Date().getTime() + 24*60*60*1000);
        agreementPayParams.put("serviceNo", "VIP");
        agreementPayParams.put("periodAmount", 1);

        Request request = new Request.Builder().url(Api.buildUrl(Api.AGREEMENT_SIGN))
                .post(RequestBody.create(
                        MediaType.parse("application/json;charset=UTF-8"),
                        JSONObject.toJSONString(agreementPayParams)))
                .headers(PayToolInfo.headers).build();

        Log.d(TAG, request.headers().toString());

        Call call = Api.getClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(AgreementActivity.this, "请求服务端失败", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().string();
                Log.d(TAG, "onResponse: " + s);

                JSONObject result = JSON.parseObject(s);

                String code = result.getString("code");

                if ("0".equals(code)) {
                    String orderInfo = "alipays://platformapi/startapp?appId=60000157&appClearTop=false&startMultApp=YES&sign_params=" + result.getJSONObject("data").getString("alipayStr");
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(orderInfo));
                    startActivity(intent);
                }
                else {
                    Looper.prepare();
                    Toast.makeText(AgreementActivity.this, result.getString("msg"),
                            Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        });
    }

    private String checkToken() {
        // check user login
        String tokenStr = PreferenceHelper.getValue(TOKEN);
        // ===========UNCOMMENT below lines, if you need authentication module=========
        if (tokenStr == null || tokenStr.isEmpty()) {
            Toast.makeText(AgreementActivity.this, "Please login before payment",
                    Toast.LENGTH_LONG).show();

            try {
                startActivity(new Intent(AgreementActivity.this, LoginActivity.class));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        return tokenStr;
    }
}
