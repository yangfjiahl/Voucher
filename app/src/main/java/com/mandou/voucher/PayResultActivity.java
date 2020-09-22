package com.mandou.voucher;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by calvin on 2019/3/8.
 */

public class PayResultActivity extends BaseActivity {

    private static final String TAG = "PayResultActivity";

    TextView resultTxt;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            JSONObject result = (JSONObject) msg.obj;
            String code = result.getString("code");

            if ("0".equals(code)) {
                JSONObject data = result.getJSONObject("data");
                resultTxt.setText(String.format("your pay result:  payChannel: %s, paymentStatus: %s", data.getString("payChannel"), data.getString("paymentStatus")));

                reportTapEvent("PAY:paid");
            } else {
                Toast.makeText(PayResultActivity.this, result.getString("msg"), Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pay_result);

        resultTxt = findViewById(R.id.pay_result_txt);

        displayPayResult();
    }

    private void displayPayResult() {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Api.buildUrl(Api.QUERY_ORDER))
                .newBuilder();
        urlBuilder.addQueryParameter("bizNo", PayToolInfo.getCurrentBizNo());

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .headers(PayToolInfo.headers)
                .build();
        Call call = Api.getClient().newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(PayResultActivity.this, "请求服务端失败", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().string();

                Log.d(TAG, "onResponse: " + s);

                JSONObject result = JSON.parseObject(s);

                Message msg = new Message();
                msg.obj = result;
                handler.sendMessage(msg);
            }
        });
    }
}
