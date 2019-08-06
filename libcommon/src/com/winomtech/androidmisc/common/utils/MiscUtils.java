package com.winomtech.androidmisc.common.utils;

import android.util.Log;

import java.io.File;

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
	
    /**
     * 获取当前线程运行的堆栈
     * @param printLine 堆栈是否需要带行号
     * @return 返回一个堆栈的字符串
     */
    public static String getStack(final boolean printLine) {
        StackTraceElement[] stes = new Throwable().getStackTrace();
        if ((stes == null) || (stes.length < 4)) {
            return "";
        }

        StringBuilder t = new StringBuilder();

        for (int i = 1; i < stes.length; i++) {
            t.append("[");
            t.append(stes[i].getClassName());
            t.append(":");
            t.append(stes[i].getMethodName());
            if (printLine) {
                t.append("(").append(stes[i].getLineNumber()).append(")]\n");
            } else {
                t.append("]\n");
            }
        }
        return t.toString();
    }
}
