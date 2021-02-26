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
import com.mandou.acp.sdk.AcpClient;
import com.mandou.acp.sdk.Agreement;
import com.mandou.acp.sdk.AgreementCallback;
import com.mandou.acp.sdk.ErrorHandler;
import com.mandou.acp.sdk.PayOrder;
import com.mandou.acp.sdk.ResultHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AgreementSdkActivity extends BaseActivity implements ErrorHandler {

    private static String TAG;

    private static final String TOKEN = "TOKEN";

    TextView tip;
    Button query_agreement;
    Button sign_agreement;
    Button unsign_agreement;
    Button pay_sign_agreement;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement_sdk);

        tip = findViewById(R.id.tip);
        query_agreement = findViewById(R.id.query_agreement);
        sign_agreement = findViewById(R.id.sign_agreement);
        unsign_agreement = findViewById(R.id.unsign_agreement);
        pay_sign_agreement = findViewById(R.id.pay_sign_agreement);

        query_agreement.setOnClickListener(v -> queryAgreement());
        sign_agreement.setOnClickListener(v -> signAgreement());
        unsign_agreement.setOnClickListener(v -> unsignAgreement());
        pay_sign_agreement.setOnClickListener(v-> payAndSign());
    }

    private PayOrder buildPayOrder(String payChannel) {
        String amountStr = "1";
        String bizNoStr = System.currentTimeMillis() + "";
        String serviceNoStr = "vip";
        String titleStr = "支付并自动代扣";

        PayOrder payOrder = PayOrder.payWith(payChannel);
        payOrder.setAmount(new BigDecimal(amountStr).multiply(new BigDecimal(100)).longValue());
        payOrder.setBizNo(bizNoStr);
        payOrder.setServiceNo(serviceNoStr);
        payOrder.setGoodsTitle(titleStr);

        String tokenStr = PreferenceHelper.getValue(TOKEN);
        if (tokenStr != null && !tokenStr.isEmpty()) {
            payOrder.setCustomerIdentity(JSONObject.parseObject(tokenStr).getString("customerId"));
        }

        Agreement agreement = new Agreement();
        agreement.setCustomerIdentity(JSONObject.parseObject(tokenStr).getString("customerId"));
        agreement.setScene("INDUSTRY|MOBILE");
        agreement.setServiceNo("VIP");
        agreement.setPeriodType("DAY");
        agreement.setPeriodValue(7);
        agreement.setPeriodAmount(1);
        agreement.setNextExecDate(new Date(2021, 3, 28));

        payOrder.setAgreement(agreement);
        return payOrder;
    }

    @Override
    public void onError(String s, String s1) {
        Toast.makeText(AgreementSdkActivity.this, s1, Toast.LENGTH_LONG).show();
    }

    private void payAndSign() {
        PayOrder payOrder = buildPayOrder("ALIPAY");
        AcpClient.sharedInstance().startPayment(AgreementSdkActivity.this, payOrder, PayResultActivity.class, AgreementSdkActivity.this);
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

        AcpClient.sharedInstance().queryAgreement(customerIdentity, "VIP", new AgreementCallback() {
            @Override
            public void onResult(Agreement agreement) {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", "agreementStatus=" + agreement.getAgreementStatus() + "\n" +
                        "nextExecDate=" + agreement.getLastExecDate().toLocaleString());
                msg.setData(bundle);
                msg.what = MSG_QUERY;
                handler.sendMessage(msg);
            }

            @Override
            public void onFail(String s, Throwable throwable) {
                Toast.makeText(AgreementSdkActivity.this, s, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void unsignAgreement() {
        String tokenStr = checkToken();
        if (tokenStr == null) return;

        String customerIdentity = JSONObject.parseObject(tokenStr).getString("customerId");

        AcpClient.sharedInstance().unsignAgreement(customerIdentity, "VIP", new ResultHandler() {
            @Override
            public void onSuccess(Object o) {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", String.valueOf(o));
                msg.setData(bundle);
                msg.what = MSG_UNSIGN;
                handler.sendMessage(msg);
            }

            @Override
            public void onError(String s, String s1) {
                Toast.makeText(AgreementSdkActivity.this, s1,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void signAgreement() {
        String tokenStr = checkToken();
        if (tokenStr == null) return;

        String customerIdentity = JSONObject.parseObject(tokenStr).getString("customerId");
        Agreement agreement = new Agreement();
        agreement.setCustomerIdentity(customerIdentity);
        agreement.setScene("INDUSTRY|MOBILE");
        agreement.setServiceNo("VIP");
        agreement.setPeriodType("DAY");
        agreement.setPeriodValue(7);
        agreement.setPeriodAmount(1);
        agreement.setNextExecDate(new Date(2021, 3, 28));

        AcpClient.sharedInstance().signAgreement(agreement, new ErrorHandler() {
            @Override
            public void onError(String s, String s1) {
                Toast.makeText(AgreementSdkActivity.this, s1, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String checkToken() {
        // check user login
        String tokenStr = PreferenceHelper.getValue(TOKEN);
        // ===========UNCOMMENT below lines, if you need authentication module=========
        if (tokenStr == null || tokenStr.isEmpty()) {
            Toast.makeText(AgreementSdkActivity.this, "Please login before payment",
                    Toast.LENGTH_LONG).show();

            try {
                startActivity(new Intent(AgreementSdkActivity.this, LoginActivity.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        return tokenStr;
    }
}
