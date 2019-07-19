package com.mandou.voucher;

import android.app.Activity;

import com.mandou.voucher.wxapi.ActionHelper;

import java.util.Date;

public class BaseActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();

        ActionModel actionModel = new ActionModel();
        actionModel.setPageName(getClass().getSimpleName());
        actionModel.setGmtOccur(new Date());
        actionModel.setPaymentPage(isPaymentPage());

        ActionHelper.reportAction(actionModel);
    }

    protected boolean isPaymentPage() {
        return false;
    }
}
