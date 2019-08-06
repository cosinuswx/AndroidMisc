package com.winomtech.androidmisc.app;

import android.app.Application;

import com.winom.olog.LogImpl;
import com.winom.olog.OLog;
import com.winomtech.androidmisc.common.constants.Constants;
import com.winomtech.androidmisc.common.cores.AmCore;
import com.winomtech.androidmisc.common.utils.AppProperties;
import com.winomtech.androidmisc.plugin.PluginManager;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOError;
import java.io.IOException;

public class MiscApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppProperties.init(this);

        try {
            FileUtils.forceMkdir(new File(Constants.SDCARD_PATH));
            FileUtils.forceMkdir(new File(Constants.LOG_SAVE_PATH));
        } catch (IOException e) {
            throw new IOError(e);
        }

        OLog.setLogImpl(new LogImpl(Constants.LOG_SAVE_PATH, "MM", ".olog"));
        OLog.setLogLevel(OLog.LEVEL_VERBOSE);
        OLog.setLogToLogcat(true);

        AmCore.initialize(this);

        // 加载插件
        PluginManager.loadPlugin(Constants.PLUGIN_JNI);
        PluginManager.loadPlugin(Constants.PLUGIN_CAMERA);
    }

    public static String getSourcePkgName() {
        return Constants.PACKAGE_NAME;
    }
}
