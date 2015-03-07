package com.winomtech.androidmisc.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.winomtech.androidmisc.R;

/**
 * @since 2015-02-13
 * @author kevinhuang 
 * 测试jni hook
 * KEVINTODO 未完待续
 */
public class JniHookFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_jni_hook, container, false);
		Button btnStart = (Button) rootView.findViewById(R.id.btn_start_test);
		btnStart.setOnClickListener(mStartTest);
		return rootView;
	}

	View.OnClickListener mStartTest = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
//			JniHook.nativeStart();
		}
	};
}
