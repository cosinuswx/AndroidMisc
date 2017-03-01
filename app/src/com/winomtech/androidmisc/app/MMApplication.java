package com.winomtech.androidmisc.app;

import android.app.Application;
import android.content.Context;

import com.winom.olog.Log;
import com.winom.olog.LogImpl;
import com.winomtech.androidmisc.plugin.PluginManager;
import com.winomtech.androidmisc.sdk.utils.MiscUtils;
import com.winomtech.androidmisc.utils.BuildInfo;
import com.winomtech.androidmisc.utils.Constants;

import java.util.Random;

/**
 * @author kevinhuang
 * @since 2015-01-20
 */
public class MMApplication extends Application {
    final static String TAG = "MMApplication";
	final static String	sourcePkgName = "com.winomtech.androidmisc";

	static Context		sContext;

	@Override
	public void onCreate() {
		super.onCreate();
		sContext = this;
        MiscUtils.mkdirs(Constants.SDCARD_PATH);
        MiscUtils.mkdirs(Constants.LOG_SAVE_PATH);

		Log.setLogImpl(new LogImpl(Constants.LOG_SAVE_PATH, "MM", ".olog"));
        Log.setLogLevel(Log.LEVEL_VERBOSE);
        Log.setLogToLogcat(true);
		registerPlugin();

        Log.i(TAG, BuildInfo.info());
	}
	
	void registerPlugin() {
		PluginManager.loadPlugin(Constants.PLUGIN_JNI);
	}
	
	public static String getSourcePkgName() {
		return sourcePkgName;
	}
	
	public static Context getContext() {
		return sContext;
	}
}
