package com.winomtech.androidmisc.app;

import android.app.Application;
import android.content.Context;

import com.winom.olog.Log;
import com.winom.olog.LogImpl;
import com.winomtech.androidmisc.common.constants.Constants;
import com.winomtech.androidmisc.common.cores.AmCore;
import com.winomtech.androidmisc.plugin.PluginManager;
import com.winomtech.androidmisc.common.utils.MiscUtils;
import com.winomtech.androidmisc.utils.BuildInfo;

/**
 * @author kevinhuang
 * @since 2015-01-20
 */
public class AmApplication extends Application {
    private final static String TAG = "MMApplication";
    private final static String sourcePkgName = "com.winomtech.androidmisc";

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        MiscUtils.mkdirs(Constants.SDCARD_PATH);
        MiscUtils.mkdirs(Constants.LOG_SAVE_PATH);

        Log.setLogImpl(new LogImpl(Constants.LOG_SAVE_PATH, "MM", ".olog"));
        Log.setLogLevel(Log.LEVEL_VERBOSE);
        Log.setLogToLogcat(true);

        AmCore.initialize(this);

        // 加载插件
        PluginManager.loadPlugin(Constants.PLUGIN_JNI);
        PluginManager.loadPlugin(Constants.PLUGIN_CAMERA);

        Log.i(TAG, BuildInfo.info());
    }

    public static String getSourcePkgName() {
        return sourcePkgName;
    }

    public static Context getContext() {
        return sContext;
    }
}
