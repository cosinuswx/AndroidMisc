package com.winomtech.androidmisc.plugin.jni;

import com.winomtech.androidmisc.sdk.plugin.IPlugin;
import com.winomtech.androidmisc.sdk.utils.Log;

/**
 * @since 2015-03-07
 * @author kevinhuang 
 */
public class Plugin implements IPlugin {
	private static final String TAG = "jni.plugin";

	static {
		new JniConstants();
	}

	@Override
	public void init() {
		Log.d(TAG, "jni plugin init");
	}
}
