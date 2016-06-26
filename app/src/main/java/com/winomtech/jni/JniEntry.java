package com.winomtech.jni;

/**
 * @since 2015-02-13
 * @author kevinhuang 
 */
public class JniEntry {
    static {
        System.loadLibrary("andoridmisc");
    }

	public static native int nativeStart();
	public static native void nativeXXTea(byte[] data, byte[] key, int isDecode);
}
