package com.winomtech.androidmisc.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.winomtech.androidmisc.R;
import com.winomtech.androidmisc.audio.PcmRecorder;
import com.winomtech.androidmisc.sdk.utils.SmTimer;
import com.winomtech.androidmisc.utils.Constants;

/**
 * @since 2015-02-05
 * @author kevinhuang
 */
public class AudioTrackRecordFragment extends Fragment {
	PcmRecorder		mPcmRecorder;
	Button			mBtnTrigger;
	ProgressBar		mProgressBar;
	SmTimer			mSmTimer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_audiotrack_record, container, false);
		mBtnTrigger = (Button) rootView.findViewById(R.id.btn_record_trigger);
		mBtnTrigger.setOnClickListener(mTriggerLsn);
		mProgressBar = (ProgressBar) rootView.findViewById(R.id.pb_voice_ampli);
		mProgressBar.setMax(65536);
		return rootView;
	}
	
	View.OnClickListener mTriggerLsn = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (null == v.getTag()) {
				mBtnTrigger.setBackgroundResource(R.drawable.selector_stop_record);
				v.setTag(1);
				mPcmRecorder = new PcmRecorder(16000, 1, Constants.WAV_FILE_PATH);
				mPcmRecorder.startRecord();
				
				mSmTimer = new SmTimer(mTimerCallback);
				mSmTimer.startIntervalTimer(0, 100);
			} else {
				mPcmRecorder.stopRecord();
				mBtnTrigger.setBackgroundResource(R.drawable.selector_start_record);
				v.setTag(null);
				mSmTimer.stopTimer();
				mSmTimer = null;
			}
		}
	};
	
	SmTimer.SmTimerCallback mTimerCallback = new SmTimer.SmTimerCallback() {
		@Override
		public void onTimeout() {
			if (null != mPcmRecorder) {
				mProgressBar.setProgress(mPcmRecorder.getAmplitude());
			}
		}
	};
}
