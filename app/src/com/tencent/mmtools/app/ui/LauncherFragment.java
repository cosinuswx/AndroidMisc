package com.tencent.mmtools.app.ui;

import com.tencent.mmtools.app.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LauncherFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_launcher, container, false);

		Button btnTraces = (Button) rootView.findViewById(R.id.btn_copy_traces);
		btnTraces.setOnClickListener(mCopyTracesLsn);
		
		Button btnAppLst = (Button) rootView.findViewById(R.id.btn_app_list);
		btnAppLst.setOnClickListener(mAppListLsn);
		return rootView;
	}

	private OnClickListener mCopyTracesLsn = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.ll_container, new CopyTracesFragment());
			transaction.addToBackStack(null);
			transaction.commit();
		}
	};
	
	private OnClickListener mAppListLsn = new OnClickListener() {
		@Override
		public void onClick(View v) {
			FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.ll_container, new PackagesFragment());
			transaction.addToBackStack(null);
			transaction.commit();
		}
	};
}
