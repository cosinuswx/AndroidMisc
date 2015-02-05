package com.winomtech.mmtools.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winomtech.mmtools.R;

/**
 * @since 2015-02-05
 * @author kevinhuang
 */
public class AudioFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_audio, container, false);
		return rootView;
	}
}
