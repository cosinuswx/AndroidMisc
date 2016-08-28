package com.winomtech.androidmisc.sdk.utils;

import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * @author kevinhuang
 */
public class MiscUtils {
	private static final String TAG = MiscUtils.class.getSimpleName();

	public static boolean mkdirs(String dir) {
		File file = new File(dir);
		if (file.exists()) {
			return file.isDirectory();
		}

		if (file.mkdirs()) {
			return true;
		} else {
			Log.e(TAG, "mkdirs failed, dir: " + dir);
			return false;
		}
	}
	
	public static void safeClose(Closeable closeable) {
		if (null != closeable) {
			try {
				closeable.close();
			} catch (IOException e) {
				Log.e(TAG, "close failed, " + e.getMessage());
			}
		}
	}
}
