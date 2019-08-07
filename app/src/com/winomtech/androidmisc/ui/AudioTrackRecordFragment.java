package com.winomtech.androidmisc.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.winom.multimedia.encoder.AudioEncoder;
import com.winom.multimedia.pipeline.StageExecutor;
import com.winom.multimedia.pipeline.StageTask;
import com.winom.multimedia.recorder.AudioRecorder;
import com.winom.multimedia.writer.Muxer;
import com.winom.olog.OLog;
import com.winomtech.androidmisc.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class AudioTrackRecordFragment extends Fragment implements StageExecutor.ExecutorListener {
    private static final String TAG = "AudioTrackRecordFragment";

    private Button mBtnTrigger;
    private AudioRecorder mAudioRecorder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_audiotrack_record, container, false);
        mBtnTrigger = rootView.findViewById(R.id.btn_record_trigger);
        mBtnTrigger.setOnClickListener(this::onClickTrigger);
        return rootView;
    }

    private void onClickTrigger(View view) {
        if (null == view.getTag()) {
            mBtnTrigger.setBackgroundResource(R.drawable.selector_stop_record);
            view.setTag(true);

            List<StageTask> tasks = new ArrayList<>();
            mAudioRecorder = new AudioRecorder(48000, 2);
            tasks.add(new StageTask("audio-recorder", mAudioRecorder));

            AudioEncoder encoder = new AudioEncoder(AudioEncoder.buildMediaFormat(48000, 2, 192 * 1024),
                    mAudioRecorder);
            tasks.add(new StageTask("audio-encoder", encoder));

            Muxer muxer = new Muxer("/sdcard/1.mp4");
            muxer.addTrackProvider(encoder.getFutureOutputFormat(), encoder);
            tasks.add(new StageTask("writer", muxer));

            StageExecutor executor = new StageExecutor(tasks, this);
            executor.start();
        } else {
            mBtnTrigger.setBackgroundResource(R.drawable.selector_start_record);
            view.setTag(null);

            mAudioRecorder.stop();
            mAudioRecorder = null;
        }
    }

    @Override
    public void onAllTaskFinished(StageExecutor executor) {
        OLog.i(TAG, "all task finished");
    }

    @Override
    public void onTaskFailed(StageExecutor executor, StageTask failedTask, Throwable error) {
    }
}
