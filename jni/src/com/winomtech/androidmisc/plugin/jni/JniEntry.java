package com.winomtech.androidmisc.plugin.jni;

/**
 * @since 2015-02-13
 * @author kevinhuang 
 */
public class JniEntry {
	public static native void XXTea(byte[] data, byte[] key, int isDecode);
}
