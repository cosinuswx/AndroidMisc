package com.winomtech.androidmisc.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.winomtech.androidmisc.R;
import com.winomtech.androidmisc.audio.PcmRecorder;
import com.winomtech.androidmisc.common.constants.Constants;
import com.winomtech.androidmisc.common.utils.SmTimer;

import androidx.fragment.app.Fragment;

public class AudioTrackRecordFragment extends Fragment {
    private PcmRecorder mPcmRecorder;
    private Button mBtnTrigger;
    private ProgressBar mProgressBar;
    private SmTimer mSmTimer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_audiotrack_record, container, false);
        mBtnTrigger = rootView.findViewById(R.id.btn_record_trigger);
        mBtnTrigger.setOnClickListener(this::onClickTrigger);
        mProgressBar = rootView.findViewById(R.id.pb_voice_ampli);
        mProgressBar.setMax(65536);
        return rootView;
    }

    private void onClickTrigger(View view) {
        if (null == view.getTag()) {
            mBtnTrigger.setBackgroundResource(R.drawable.selector_stop_record);
            view.setTag(true);
            mPcmRecorder = new PcmRecorder(16000, 1, Constants.WAV_FILE_PATH);
            mPcmRecorder.startRecord();

            mSmTimer = new SmTimer(mTimerCallback);
            mSmTimer.startIntervalTimer(0, 100);
        } else {
            mPcmRecorder.stopRecord();
            mBtnTrigger.setBackgroundResource(R.drawable.selector_start_record);
            view.setTag(null);
            mSmTimer.stopTimer();
            mSmTimer = null;
        }
    }

    private SmTimer.SmTimerCallback mTimerCallback = new SmTimer.SmTimerCallback() {
        @Override
        public void onTimeout() {
            if (null != mPcmRecorder) {
                mProgressBar.setProgress(mPcmRecorder.getAmplitude());
            }
        }
    };
}
