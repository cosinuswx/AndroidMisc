package com.winomtech.androidmisc.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.winom.olog.Log;
import com.winomtech.androidmisc.R;
import com.winomtech.androidmisc.app.MMApplication;
import com.winomtech.androidmisc.utils.BuildInfo;

public class MainActivity extends ActionBarActivity {
	private final static String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		getSupportFragmentManager().beginTransaction().replace(R.id.ll_container, new LauncherFragment()).commit();
	}

}
