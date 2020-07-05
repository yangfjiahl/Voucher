/**
 * Copyright (C) 2019 ~ 2020 itech.com. All Rights Reserved.
 */
package com.mandou.voucher;

import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mandou.acp.sdk.AcpClient;
import com.mandou.acp.sdk.AuthCallback;
import com.mandou.acp.sdk.ErrorHandler;
import com.mandou.acp.sdk.PayOrder;
import com.mandou.acp.sdk.PayOrderCallback;
import com.mandou.acp.sdk.PayOrderInfo;
import com.mandou.acp.sdk.PayToolCallback;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: YangFeng(calvin)
 * @date: 2020/7/4 23:02
 * @description:
 * @version: v1.0
 */
public class PayActivity extends BaseActivity implements ErrorHandler {

    EditText amount;
    EditText bizNo;
    EditText goodsTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pay);

        AcpClient.sharedInstance().initPayTools(new PayToolCallback() {

            @Override
            public void onSuccess(String s) {
                if ("WECHAT".equalsIgnoreCase(s)) {
                    initWechat();
                } else if ("ALIPAY".equalsIgnoreCase(s)) {
                    initAlipay();
                }
            }

            @Override
            public void onFail(String s, Throwable throwable) {
                Looper.prepare();
                Toast.makeText(PayActivity.this, "支付环境初始化失败", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        });

        AcpClient.sharedInstance().setLoggingEnabled();

        amount = findViewById(R.id.amount);
        bizNo = findViewById(R.id.bizNo);
        goodsTitle = findViewById(R.id.title);

        initSms();
        initCode();

        queryHistory();
    }

    // ======================支付相关
    Button wechatBtn;

    private void initWechat() {
        wechatBtn = findViewById(R.id.btn_pay_wechat);
        wechatBtn.setVisibility(View.VISIBLE);

        wechatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PayOrder payOrder = buildPayOrder("WECHAT");

                AcpClient.sharedInstance().startPayment(PayActivity.this, payOrder, null, PayActivity.this);
            }
        });
    }

    private PayOrder buildPayOrder(String payChannel) {
        String amountStr = amount.getText().toString();
        String bizNoStr = bizNo.getText().toString();
        String titleStr = goodsTitle.getText().toString();

        if (amountStr.length() == 0 || bizNoStr.length() == 0 || titleStr.length() == 0) {
            Toast.makeText(PayActivity.this, "Please input bizNo、amount and title", Toast.LENGTH_LONG).show();
            return null;
        }

        PayOrder payOrder = PayOrder.payWith(payChannel);
        payOrder.setAmount(new BigDecimal(amountStr).multiply(new BigDecimal(100)).longValue());
        payOrder.setBizNo(bizNoStr);
        payOrder.setGoodsTitle(titleStr);

        return payOrder;
    }

    Button alipayBtn;

    private void initAlipay() {
        alipayBtn = findViewById(R.id.btn_pay_alipay);
        alipayBtn.setVisibility(View.VISIBLE);

        alipayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PayOrder payOrder = buildPayOrder("ALIPAY");

                AcpClient.sharedInstance().startPayment(PayActivity.this, payOrder, PayResultActivity.class, PayActivity.this);
            }
        });
    }

    @Override
    public void onError(String code, String msg) {
        Toast.makeText(PayActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    // ====================支付相关

    // ====================登录相关
    private void checkAuth() {
        AcpClient.sharedInstance().checkAuth("{'token':'123'}", new AuthCallback() {
            @Override
            public void onResult(boolean b, Map<String, String> map) {

            }

            @Override
            public void onFail(String s, Throwable throwable) {

            }
        });
    }

    Button smsBtn;
    EditText mobileNo;

    private void initSms() {
        checkAuth();
        smsBtn = findViewById(R.id.smsBtn);
        mobileNo = findViewById(R.id.mobileText);

        smsBtn.setOnClickListener(v -> {
            String mobileNoStr = mobileNo.getText().toString();
            if (mobileNoStr == null || mobileNoStr.length() == 0) {
                Toast.makeText(PayActivity.this, "手机号必填", Toast.LENGTH_LONG).show();
                return;
            }

            AcpClient.sharedInstance().sendSms(mobileNoStr, new AuthCallback() {
                @Override
                public void onResult(boolean b, Map<String, String> data) {

                }

                @Override
                public void onFail(String s, Throwable throwable) {

                }
            });
        });
    }

    Button codeBtn;
    EditText code;

    private void initCode() {
        codeBtn = findViewById(R.id.loginBtn);
        code = findViewById(R.id.codeText);

        codeBtn.setOnClickListener(v -> {
            String codeStr = code.getText().toString();
            if (codeStr == null || codeStr.length() == 0) {
                Toast.makeText(PayActivity.this, "验证码必填", Toast.LENGTH_LONG).show();
                return;
            }

            String mobileNoStr = mobileNo.getText().toString();

            AcpClient.sharedInstance().checkSmsCode(mobileNoStr, codeStr, new AuthCallback() {
                @Override
                public void onResult(boolean b, Map<String, String> data) {

                }

                @Override
                public void onFail(String s, Throwable throwable) {

                }
            });
        });
    }

    // ======================历史订单相关
    private void queryHistory() {
        AcpClient.sharedInstance().queryHistoryOrder("20190401000082726316100012", "PAID", 1, 10, new PayOrderCallback() {
            @Override
            public void onSuccess(List<PayOrderInfo> list) {

            }

            @Override
            public void onFail(String s, Throwable throwable) {

            }
        });

        AcpClient.sharedInstance().querySingleOrder("20190401000082726316100012", "dhshehx", new PayOrderCallback() {
            @Override
            public void onSuccess(List<PayOrderInfo> list) {

            }

            @Override
            public void onFail(String s, Throwable throwable) {

            }
        });
    }
}