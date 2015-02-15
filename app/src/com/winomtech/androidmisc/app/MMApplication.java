package com.winomtech.androidmisc.app;

import android.app.Application;

import com.winomtech.androidmisc.jni.JniConstants;
import com.winomtech.androidmisc.utils.Constants;
import com.winomtech.androidmisc.utils.Log;
import com.winomtech.androidmisc.utils.MiscUtils;

/**
 * @author kevinhuang
 * @since 2015-01-20
 */
public class MMApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		new JniConstants();
		Log.initImpl("MM");
		MiscUtils.mkdirs(Constants.SDCARD_PATH);
	}
}
