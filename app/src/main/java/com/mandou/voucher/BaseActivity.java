package com.mandou.voucher;

import android.app.Activity;

import java.util.Map;

public class BaseActivity extends Activity implements HasPayment {

    @Override
    public boolean isPaymentPage() {
        return false;
    }

    protected void reportTapEvent(String eventName) {
        ActionModel actionModel = new ActionModel();
        actionModel.setActionType("TAP");
        actionModel.setPageName(getClass().getSimpleName());
        actionModel.setPaymentPage(isPaymentPage());
        actionModel.setEventName(eventName);
        ActionHelper.reportAction(actionModel);
    }

    protected void reportTapEvent(String eventName, Map<String, Object> extra) {
        ActionModel actionModel = new ActionModel();
        actionModel.setActionType("TAP");
        actionModel.setPageName(getClass().getSimpleName());
        actionModel.setPaymentPage(isPaymentPage());
        actionModel.setEventName(eventName);
        actionModel.setAttachData(extra);
        ActionHelper.reportAction(actionModel);
    }
}
