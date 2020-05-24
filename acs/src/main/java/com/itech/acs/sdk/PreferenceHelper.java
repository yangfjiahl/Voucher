package com.itech.acs.sdk;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by calvin on 2019/3/30.
 */

public class PreferenceHelper {

    private static Context context;

    public static void init(Context context) {
        PreferenceHelper.context = context;
    }

    public static String getValue(String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, null);
    }

    public static void setValue(String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).apply();
    }

    public static void remove(String key) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(key).apply();
    }
}
