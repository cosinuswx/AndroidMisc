package com.winomtech.androidmisc.common.constants;

import android.os.Environment;

public class Constants {
    public static final String PACKAGE_NAME = "com.winom.androidmisc";
    public final static String SDCARD_PATH   = Environment.getExternalStorageDirectory() + "/AndroidMisc";
    public final static String WAV_FILE_PATH = SDCARD_PATH + "/test.wav";
    public final static String LOG_SAVE_PATH = SDCARD_PATH + "/logs";

    // subcore名字定义
    public static final String SUBCORE_APP    = "subcoreapp";
    public static final String SUBCORE_CAMERA = "subcorecamera";

    // 插件名字定义
    public final static String PLUGIN_JNI    = "jni";
    public static final String PLUGIN_CAMERA = "camera";
}
