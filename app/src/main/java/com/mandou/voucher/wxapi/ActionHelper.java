package com.mandou.voucher.wxapi;

import android.content.Context;

import com.mandou.voucher.ActionModel;
import com.mandou.voucher.SessionHelper;

public class ActionHelper {

    private static String TAG = "Action";

    private static Context context;

    public static void init(Context context) {
        ActionHelper.context = context;
    }

    public static void reportAction(ActionModel actionModel) {

    }
}
