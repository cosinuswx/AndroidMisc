package com.winomtech.androidmisc.jni;

/**
 * @since 2015-02-13
 * @author kevinhuang
 */
public class JniConstants {
	static {
		System.loadLibrary("andoridmisc");
	}
}
