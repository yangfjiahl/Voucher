package com.mandou.voucher.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.mandou.voucher.MainActivity;
import com.mandou.voucher.PayResult;
import com.mandou.voucher.PayResultActivity;
import com.mandou.voucher.PayToolInfo;
import com.mandou.voucher.R;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by calvin on 2019/2/26.
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "WXPayEntryActivity";

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);

        Log.d(TAG, "appId=" + PayToolInfo.getWechatAppId());

        api = WXAPIFactory.createWXAPI(this, PayToolInfo.getWechatAppId());
        api.handleIntent(getIntent(), this);

        Log.d(TAG, "pay create");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);

        Log.d(TAG, "pay new intent");
    }

    @Override
    public void onReq(BaseReq req) {
        Log.d(TAG, "request ");
    }

    @Override
    public void onResp(BaseResp resp) {
        Log.d(TAG, "onPayFinish, errCode = " + resp.errStr + " type=" + resp.getType() + " transaction=" + resp.transaction);

        startActivity(new Intent(this, PayResultActivity.class));
        finish();
    }
}
