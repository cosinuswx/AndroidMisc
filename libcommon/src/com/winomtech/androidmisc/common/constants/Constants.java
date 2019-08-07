package com.winomtech.androidmisc.common.constants;

import android.os.Environment;

import java.util.concurrent.TimeUnit;

public interface Constants {
    String PACKAGE_NAME = "com.winom.androidmisc";
    String SDCARD_PATH   = Environment.getExternalStorageDirectory() + "/AndroidMisc";
    String WAV_FILE_PATH = SDCARD_PATH + "/test.wav";
    String LOG_SAVE_PATH = SDCARD_PATH + "/logs";

    // subcore名字定义
    String SUBCORE_CAMERA = "subcorecamera";

    // 插件名字定义
    String PLUGIN_JNI    = "jni";
    String PLUGIN_CAMERA = "camera";

    long MS_PER_SECOND = TimeUnit.SECONDS.toMillis(1);
}
