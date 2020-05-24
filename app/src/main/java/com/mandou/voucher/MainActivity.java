package com.mandou.voucher;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sdk.app.PayTask;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends BaseActivity {

    private static String TAG;

    private static final String TOKEN = "TOKEN";

    private IWXAPI api;

    EditText amount;
    EditText bizNo;
    EditText goodsTitle;

    Button btnTap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPayTools();

        amount = findViewById(R.id.amount);
        bizNo = findViewById(R.id.bizNo);
        goodsTitle = findViewById(R.id.title);
        btnTap = findViewById(R.id.btn_tap);

        btnTap.setOnClickListener(v -> {
            reportTapEvent("btn_click");
        });

        TAG = getClass().getSimpleName();

        requestPermission();
    }

    private static final int PERMISSIONS_REQUEST_CODE = 1002;

    private void requestPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                ) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, PERMISSIONS_REQUEST_CODE);

        }
    }

    private static final int MSG_SHOW_WECHAT = 1;
    private static final int MSG_SHOW_ALIPAY = 2;

    private static final int MSG_CREATE_ORDER_RESP = 3;

    private static final int MSG_ALI_PAID = 4;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            String appId = msg.getData().getString("appId");

            switch (msg.what) {
                case MSG_SHOW_WECHAT:
                    initWechatPay(appId);
                    break;
                case MSG_SHOW_ALIPAY:
                    initAliPay(appId);
                    break;
                case MSG_CREATE_ORDER_RESP:
                    callPay(msg.getData());
                    break;
                case MSG_ALI_PAID:
                    startActivity(new Intent(MainActivity.this, PayResultActivity.class));
                    break;
            }
        }
    };

    private void callPay(Bundle bundle) {
        JSONObject data = (JSONObject) bundle.getSerializable("data");

        if ("WECHAT".equalsIgnoreCase(data.getString("payChannel"))) {
            callWechatPay(data);
        } else {
            callAlipay(data);
        }
    }

    private void callAlipay(JSONObject data) {
        String sign = data.getString("sign");
        data.remove("sign");
        data.remove("payChannel");
//        data.remove("format");

        StringBuffer sb = new StringBuffer();
        for (String k : data.keySet()) {
            sb.append(k);
            sb.append('=');
            try {
                sb.append(URLEncoder.encode(data.getString(k), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            sb.append('&');
        }

        try {
            sb.append("sign=").append(URLEncoder.encode(sign, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final String orderInfo = sb.toString();

        Log.d(TAG, orderInfo);

        new Thread(new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask(MainActivity.this);
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Log.d(TAG, "支付宝支付结果:" + result);
                Message msg = new Message();
                msg.what = MSG_ALI_PAID;
                msg.obj = result;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private void callWechatPay(JSONObject data) {
        // {appid=wx7f229e38a04f2bec, noncestr=LXyHXBUVh39LYBrK, package=Sign=WXPay, partnerid=1523808161, prepayid=wx2613502950188989bf5937ec1163319167, sign=50F044924686609080E94B7C6EB7969E, timestamp=1551160229}
        PayReq req = new PayReq();
        req.appId = data.getString("appid");
        req.partnerId = data.getString("partnerid");
        req.prepayId = data.getString("prepayid");
        req.nonceStr = data.getString("noncestr");
        req.timeStamp = data.getString("timestamp");
        req.packageValue = data.getString("package");
        req.sign = data.getString("sign");

        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
        //3.调用微信支付sdk支付方法
        Log.d(TAG, req.checkArgs() + " 检查结果");
        boolean v = api.sendReq(req);
        Log.d(TAG, v + " 启动结果");
    }

    private void initPayTools() {
        Request request = new Request.Builder()
                .url(Api.buildUrl(Api.GET_PAY_TOOLS))
                .headers(PayToolInfo.headers)
                .build();

        Log.d(TAG, request.headers().toString());

        Call call = Api.getClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(MainActivity.this, "请求服务端失败", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().string();
                Log.d(TAG, "onResponse: " + s);

                JSONObject result = JSON.parseObject(s);

                String code = result.getString("code");

                if ("0".equals(code)) {
                    doInitPayTools(result.getJSONArray("data"));
                } else {
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, result.getString("msg"), Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        });
    }

    private void doInitPayTools(JSONArray payToolList) {
        if (payToolList == null || payToolList.size() == 0) {
            Toast.makeText(MainActivity.this, "您的APP还未在平台登记", Toast.LENGTH_LONG).show();
            return;
        }

        for (int i = 0; i < payToolList.size(); i++) {
            JSONObject payTool = payToolList.getJSONObject(i);
            String appId = payTool.getString("id");
            String payChannel = payTool.getString("payChannel");

            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("appId", appId);
            msg.setData(bundle);

            if ("WECHAT".equalsIgnoreCase(payChannel)) {
                msg.what = MSG_SHOW_WECHAT;
            } else if ("ALIPAY".equalsIgnoreCase(payChannel)) {
                msg.what = MSG_SHOW_ALIPAY;
            }

            handler.sendMessage(msg);
        }
    }

    /**
     * 初始化微信支付
     *
     * @param appId
     */
    private void initWechatPay(String appId) {
        PayToolInfo.setWechatAppId(appId);

        api = WXAPIFactory.createWXAPI(this, appId);

        Button button = findViewById(R.id.btn_pay_wechat);
        button.setVisibility(View.VISIBLE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPayRequest("WECHAT");
            }
        });
    }

    private void initAliPay(String appId) {
        Button button = findViewById(R.id.btn_pay_alipay);
        button.setVisibility(View.VISIBLE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPayRequest("ALIPAY");
            }
        });
    }

    private void sendPayRequest(final String payChannel) {

        String amountStr = amount.getText().toString();
        String bizNoStr = bizNo.getText().toString();
        String titleStr = goodsTitle.getText().toString();

        if (amountStr.length() == 0 || bizNoStr.length() == 0 || titleStr.length() == 0) {
            Toast.makeText(MainActivity.this, "Please input bizNo、amount and title", Toast.LENGTH_LONG).show();
            return;
        }

        // check user login
        String tokenStr = PreferenceHelper.getValue(TOKEN);
        // ===========UNCOMMENT below lines, if you need authentication module=========
        if (tokenStr == null || tokenStr.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please login before payment", Toast.LENGTH_LONG).show();

            try {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        Map<String, Object> extra = new HashMap<>();
        extra.put("payChannel", payChannel);
        reportTapEvent("payment", extra);

        PayToolInfo.setCurrentBizNo(bizNoStr);

        Map<String, Object> params = new HashMap<>();
        params.put("amount", new BigDecimal(amountStr).multiply(new BigDecimal(100)).longValue());
        params.put("bizNo", bizNoStr);
        params.put("goodsTitle", titleStr);
        params.put("payChannel", payChannel);

        if (tokenStr != null && !tokenStr.isEmpty()) {
            params.put("customerIdentity", JSONObject.parseObject(tokenStr).getString("customerId"));
        }

        Request request = new Request.Builder()
                .url(Api.buildUrl(Api.CREATE_ORDER))
                .post(RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), JSONObject.toJSONString(params)))
                .headers(PayToolInfo.headers)
                .build();
        Call call = Api.getClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(MainActivity.this, "请求服务端失败", Toast.LENGTH_LONG).show();
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
                    data.put("payChannel", payChannel);

                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("data", data);
                    msg.setData(bundle);
                    msg.what = MSG_CREATE_ORDER_RESP;
                    handler.sendMessage(msg);
                } else {
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, result.getString("msg"), Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        });
    }

    @Override
    public boolean isPaymentPage() {
        return true;
    }
}
