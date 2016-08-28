package com.winomtech.androidmisc.ui;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.winomtech.androidmisc.R;
import com.winomtech.androidmisc.sdk.utils.SmTimer;
import com.winomtech.androidmisc.utils.Constants;

import java.io.IOException;
import java.util.Locale;

/**
 * @since 2015-02-06
 * @author kevinhuang 
 */
public class MediaRecorderAudioFragment extends Fragment {
	Button 			mBtnTrigger;
	TextView		mTvVoiceLen;
	MediaRecorder	mMediaRecorder;
	long			mStartTick = 0;
	SmTimer			mSmTimer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_mediorecorder_audio, container, false);
		mBtnTrigger = (Button) rootView.findViewById(R.id.btn_record_trigger);
		mBtnTrigger.setOnClickListener(mTriggerLsn);
		mTvVoiceLen = (TextView) rootView.findViewById(R.id.tv_voice_length);
		mTvVoiceLen.setText("00:00");
		return rootView;
	}
	
	View.OnClickListener	mTriggerLsn = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (null == v.getTag()) {
				v.setTag(1);
				mBtnTrigger.setBackgroundResource(R.drawable.selector_stop_record);
				
				mMediaRecorder = new MediaRecorder();
				mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
				mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
				mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
				mMediaRecorder.setOutputFile(Constants.WAV_FILE_PATH);
				try {
					mMediaRecorder.prepare();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				mMediaRecorder.start();
				mStartTick = System.currentTimeMillis();
				
				mSmTimer = new SmTimer(mTimerCallback);
				mSmTimer.startIntervalTimer(0, 500);
			} else {
				v.setTag(null);
				mBtnTrigger.setBackgroundResource(R.drawable.selector_start_record);
				mMediaRecorder.stop();
				mMediaRecorder.release();
				mSmTimer.stopTimer();
			}
		}
	};

	SmTimer.SmTimerCallback	mTimerCallback = new SmTimer.SmTimerCallback() {
		@Override
		public void onTimeout() {
			long voiceLen = (System.currentTimeMillis() - mStartTick) / 1000;
			mTvVoiceLen.setText(String.format(Locale.US, "%02d:%02d", voiceLen / 60, voiceLen % 60));
		}
	};
}
