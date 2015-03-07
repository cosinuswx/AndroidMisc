package com.winomtech.androidmisc.app;

import android.app.Application;

import com.winomtech.androidmisc.jni.JniConstants;
import com.winomtech.androidmisc.sdk.utils.Log;
import com.winomtech.androidmisc.utils.Constants;
import com.winomtech.androidmisc.sdk.utils.MiscUtils;

/**
 * @author kevinhuang
 * @since 2015-01-20
 */
public class MMApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		registerPlugin();
		new JniConstants();
		Log.initImpl(Constants.LOG_SAVE_PATH, "MM");
		MiscUtils.mkdirs(Constants.SDCARD_PATH);
	}
	
	void registerPlugin() {
		
	}
}
