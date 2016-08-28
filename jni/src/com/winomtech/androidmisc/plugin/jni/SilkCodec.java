package com.winomtech.androidmisc.plugin.jni;

public class SilkCodec {

	private long mNativeRecorderInJavaObj;

	public SilkCodec() {
	}

	public void init(String strPath) {
		nativeInit(strPath);
	}

	public void doDec(char[] buf, int len) {
		nativeDoDec(buf, len);
	}

	public void release() {
		nativeRelease();
	}

	private native void nativeInit(String strPath);
	private native void nativeDoDec(char[] buf, int len);
	private native void nativeRelease();
}
