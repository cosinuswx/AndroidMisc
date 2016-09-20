package com.winomtech.androidmisc.app;

import android.app.Application;
import android.content.Context;
import android.nfc.Tag;

import com.winomtech.androidmisc.plugin.PluginManager;
import com.winomtech.androidmisc.sdk.utils.Log;
import com.winomtech.androidmisc.sdk.utils.MiscUtils;
import com.winomtech.androidmisc.utils.BuildInfo;
import com.winomtech.androidmisc.utils.Constants;

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
		Log.initImpl(Constants.LOG_SAVE_PATH, "MM");
		registerPlugin();
		MiscUtils.mkdirs(Constants.SDCARD_PATH);

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
