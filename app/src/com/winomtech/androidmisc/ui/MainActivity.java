package com.winomtech.androidmisc.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.winomtech.androidmisc.R;

public class MainActivity extends FragmentActivity {
	private final static String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		getSupportFragmentManager().beginTransaction().replace(R.id.ll_container, new LauncherFragment()).commit();
	}

}
