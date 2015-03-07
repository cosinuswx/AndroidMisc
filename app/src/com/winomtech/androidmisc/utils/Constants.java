package com.winomtech.androidmisc.utils;

import android.os.Environment;

/**
 * @author kevinhuang
 * @since 2015-01-20
 */
public class Constants {
	public final static String	SDCARD_PATH		= Environment.getExternalStorageDirectory() + "/AndroidMisc";
	public final static String	WAV_FILE_PATH	= SDCARD_PATH + "/test.wav";
	public final static String	LOG_SAVE_PATH	= SDCARD_PATH + "/logs";

	public final static String	PLUGIN_JNI		= "jni";
}
