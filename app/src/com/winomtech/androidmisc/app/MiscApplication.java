package com.winomtech.androidmisc.app;

import android.app.Application;

import com.winom.olog.LogImpl;
import com.winom.olog.OLog;
import com.winomtech.androidmisc.common.constants.Constants;
import com.winomtech.androidmisc.common.cores.AmCore;
import com.winomtech.androidmisc.common.utils.AppProperties;
import com.winomtech.androidmisc.common.utils.MiscUtils;
import com.winomtech.androidmisc.plugin.PluginManager;

public class MiscApplication extends Application {
    private final static String sourcePkgName = "com.winomtech.androidmisc";

    @Override
    public void onCreate() {
        super.onCreate();
        AppProperties.init(this);

        MiscUtils.mkdirs(Constants.SDCARD_PATH);
        MiscUtils.mkdirs(Constants.LOG_SAVE_PATH);

        OLog.setLogImpl(new LogImpl(Constants.LOG_SAVE_PATH, "MM", ".olog"));
        OLog.setLogLevel(OLog.LEVEL_VERBOSE);
        OLog.setLogToLogcat(true);

        AmCore.initialize(this);

        // 加载插件
        PluginManager.loadPlugin(Constants.PLUGIN_JNI);
        PluginManager.loadPlugin(Constants.PLUGIN_CAMERA);
    }

    public static String getSourcePkgName() {
        return sourcePkgName;
    }
}
