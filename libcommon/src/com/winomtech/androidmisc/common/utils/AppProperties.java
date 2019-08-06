package com.winomtech.androidmisc.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;

public class AppProperties {
    @SuppressLint("StaticFieldLeak")
    private static AppProperties sAppProperties;

    public static void init(Context appContext) {
        sAppProperties = new AppProperties(appContext);
    }

    public static AppProperties getInstance() {
        if (sAppProperties == null) {
            throw new RuntimeException("Please init AppProperties first!");
        }
        return sAppProperties;
    }

    private final Context mAppContext;

    private AppProperties(Context appContext) {
        mAppContext = appContext;
    }

    public Context getAppContext() {
        return mAppContext;
    }
}
