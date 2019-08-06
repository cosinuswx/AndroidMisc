package com.winomtech.androidmisc.ui;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.winomtech.androidmisc.R;
import com.winomtech.androidmisc.common.constants.Constants;
import com.winomtech.androidmisc.common.utils.SmTimer;

import java.io.IOException;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class MediaRecorderAudioFragment extends Fragment {
    private Button mBtnTrigger;
    private TextView mTvVoiceLen;
    private MediaRecorder mMediaRecorder;
    private long mStartTick = 0;
    private SmTimer mSmTimer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mediorecorder_audio, container, false);
        mBtnTrigger = rootView.findViewById(R.id.btn_record_trigger);
        mBtnTrigger.setOnClickListener(this::onClickTrigger);
        mTvVoiceLen = rootView.findViewById(R.id.tv_voice_length);
        mTvVoiceLen.setText("00:00");
        return rootView;
    }

    private void onClickTrigger(View view) {
        if (null == view.getTag()) {
            view.setTag(true);
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
            view.setTag(null);
            mBtnTrigger.setBackgroundResource(R.drawable.selector_start_record);
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mSmTimer.stopTimer();
        }
    }

    private SmTimer.SmTimerCallback mTimerCallback = new SmTimer.SmTimerCallback() {
        @Override
        public void onTimeout() {
            long voiceLen = (System.currentTimeMillis() - mStartTick) / 1000;
            mTvVoiceLen.setText(String.format(Locale.US, "%02d:%02d", voiceLen / 60, voiceLen % 60));
        }
    };
}
