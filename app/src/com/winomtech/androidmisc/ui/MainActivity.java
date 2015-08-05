package com.winomtech.androidmisc.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;

import com.winomtech.androidmisc.R;
import com.winomtech.androidmisc.sdk.utils.Log;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends ActionBarActivity {
	private final static String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		getSupportFragmentManager().beginTransaction().replace(R.id.ll_container, new LauncherFragment()).commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		String strValue = "12345678";
		String strKey = "1234567890123456";
		try {
			byte[] key = strKey.getBytes("utf-8");
			Log.d(TAG, "key: " + byteArrayToString(key));
			byte[] value = strValue.getBytes("utf-8");
			Log.d(TAG, "1: " + byteArrayToString(value));
			int alignLen = (4 - (value.length % 4)) % 4;
			ByteBuffer byteBuffer = ByteBuffer.allocate(4 + value.length + alignLen);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			byteBuffer.putInt(value.length);
			byteBuffer.put(value);
			for (int i = 0; i < alignLen; ++i) {
				byteBuffer.put((byte) 0);
			}
			byte[] data = byteBuffer.array();
			Log.d(TAG, "2: " + byteArrayToString(data));
//			JniHook.nativeXXTea(data, key, 0);
			Log.d(TAG, "3: " + byteArrayToString(data));

			Log.d(TAG, Base64.encodeToString(data, 0));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();

		try {
			String strKey = "1234567890123456";
			byte[] key = strKey.getBytes("utf-8");
			Log.d(TAG, "key: " + byteArrayToString(key));
			byte[] value = Base64.decode("0ScoyhgASTfETRhR", 0);
			Log.d(TAG, "decode, value: " + byteArrayToString(value));

//			JniHook.nativeXXTea(value, key, 1);
			Log.d(TAG, "deocde end, value: " + byteArrayToString(value));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	static String byteArrayToString(byte[] arr) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < arr.length; ++i) {
			builder.append(" ");
			builder.append(byte2hexChar((byte) ((arr[i] & 0xf0) >> 4)));
			builder.append(byte2hexChar((byte) (arr[i] & 0xf)));
		}
		return builder.toString();
	}

	static char byte2hexChar(byte val) {
		return (char) (val <= 9 ? val + '0' : val - 10 + 'a');
	}

	public static String byteToHexStr(byte[] data) {
		if (null == data) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < data.length; ++i) {
			int val = data[i] & 0xff;
			builder.append(byte2hexChar((byte) (val / 16)));
			builder.append(byte2hexChar((byte) (val % 16)));
		}
		return builder.toString();
	}
}
