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

    public static byte[] hexStrToByte(String hexStr) {
        if (null == hexStr) {
            return null;
        }

        if ((hexStr.length() % 2) != 0) {
            throw new RuntimeException("hex string must be in multiple of 2");
        }

        byte[] byteArr = new byte[hexStr.length() / 2];
        for (int i = 0; i < byteArr.length; ++i) {
            byteArr[i] = (byte) ((hexChar2byte(hexStr.charAt(2 * i)) << 4) | hexChar2byte(hexStr.charAt(2 * i + 1)));
        }
        return byteArr;
    }

    static char byte2hexChar(byte val) {
        return (char) (val <= 9 ? val + '0' : val - 10 + 'a');
    }

    static byte hexChar2byte(char c) {
        if (c >= 'A' && c <= 'Z') {
            c = (char) (c - 'A' + 'a');
        }

        return (byte) ((c >= '0' && c <= '9') ? c - '0' : c - 'a' + 10);
    }

    /**
     * 加密byte数组
     * @param data 需要加密的数组，在输入的数据上面直接进行加密
     * @param key 密码
     */
    public static void xorByteArray(byte[] data, int offset, int length, byte[] key) {
        for (int i = offset, j = 0; i < offset + length; ++i, ++j) {
            if (j == key.length) {
                j = 0;
            }
            data[i] ^= key[j];
        }
    }
}
