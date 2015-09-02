package com.winomtech.androidmisc.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;

import com.winomtech.androidmisc.R;
import com.winomtech.androidmisc.plugin.jni.JniEntry;
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

}
