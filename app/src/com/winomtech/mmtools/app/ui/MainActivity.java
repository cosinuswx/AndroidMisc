package com.winomtech.mmtools.app.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.winomtech.mmtools.app.R;

public class MainActivity extends ActionBarActivity {
	private final static String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		getSupportFragmentManager().beginTransaction().replace(R.id.ll_container, new LauncherFragment()).commit();
	}

}
