package com.mandou.voucher;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private IWXAPI api;

    private static final String APP_ID = "20190303002";
    private static final String APP_SECRET = "123";
    private static final Headers headers;

    private static String TAG;

    static {
        headers = Headers.of("APP_ID", APP_ID, "APP_SECRET", APP_SECRET);
    }

    EditText amount;
    EditText bizNo;
    EditText goodsTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPayTools();

        amount = findViewById(R.id.amount);
        bizNo = findViewById(R.id.bizNo);
        goodsTitle = findViewById(R.id.title);

        TAG = getClass().getSimpleName();
    }

    private static final int MSG_SHOW_WECHAT = 1;
    private static final int MSG_SHOW_ALIPAY = 2;

    private static final int MSG_PAY_WECHAT = 3;
    private static final int MSG_PAY_ALIPAY = 4;

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
                case MSG_PAY_WECHAT:
                    wechatPay(msg.getData());
                    break;
                case MSG_PAY_ALIPAY:
//                    alipay(msg.getData());
                    break;
            }
        }
    };

    private void wechatPay(Bundle bundle) {
        JSONObject data = (JSONObject) bundle.getSerializable("data");
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
                .headers(headers)
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

    private void initWechatPay(String appId) {
        PayToolInfo.setWechatAppId(appId);

        api = WXAPIFactory.createWXAPI(getApplicationContext(), null);

        boolean v =  api.registerApp(appId);
        Log.d(TAG, v + " 注册结果");

        Button button = findViewById(R.id.btn_pay_wechat);
        button.setVisibility(View.VISIBLE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendWechatPayRequest();
            }
        });
    }

    private void initAliPay(String appId) {
        Button button = findViewById(R.id.btn_pay_alipay);
        button.setVisibility(View.VISIBLE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                sendAliPayRequest();
            }
        });
    }

    public void sendWechatPayRequest() {
        String amountStr = amount.getText().toString();
        String bizNoStr = bizNo.getText().toString();
        String titleStr = goodsTitle.getText().toString();

        if (amountStr.length() == 0 || bizNoStr.length() == 0 || titleStr.length() == 0) {
            Toast.makeText(MainActivity.this, "请输入金额、单号、标题", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("amount", new BigDecimal(amountStr).multiply(new BigDecimal(100)).longValue());
        params.put("bizNo", bizNoStr);
        params.put("goodsTitle", titleStr);
        params.put("payChannel", "WECHAT");

        Request request = new Request.Builder()
                .url(Api.buildUrl(Api.CREATE_ORDER))
                .post(RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), JSONObject.toJSONString(params)))
                .headers(headers)
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

                JSONObject data = result.getJSONObject("data");

                if ("0".equals(code)) {
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("data", data);
                    msg.setData(bundle);
                    msg.what = MSG_PAY_WECHAT;
                    handler.sendMessage(msg);
                } else {
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "服务端故障，请联系客服", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        });
    }
}