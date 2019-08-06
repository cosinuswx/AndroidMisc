package com.winomtech.androidmisc.ui;

import android.os.Bundle;

import com.winomtech.androidmisc.R;

import androidx.fragment.app.FragmentActivity;

public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		getSupportFragmentManager().beginTransaction().replace(R.id.ll_container, new LauncherFragment()).commit();
	}

}
