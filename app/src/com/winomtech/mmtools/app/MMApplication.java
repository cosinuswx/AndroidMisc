package com.winomtech.mmtools.app;

import android.app.Application;

import com.winomtech.mmtools.utils.Constants;
import com.winomtech.mmtools.utils.Log;
import com.winomtech.mmtools.utils.MiscUtils;

/**
 * @author kevinhuang
 * @since 2015-01-20
 */
public class MMApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Log.initImpl("MM");
		MiscUtils.mkdirs(Constants.SDCARD_PATH);
	}
}
