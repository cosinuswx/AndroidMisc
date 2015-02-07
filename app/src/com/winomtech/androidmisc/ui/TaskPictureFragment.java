package com.winomtech.androidmisc.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winomtech.androidmisc.R;

/**
 * @since 2015-02-07
 * @author kevinhuang
 */
public class TaskPictureFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_take_picture, container, false);
		return rootView;
	}
}
