package com.winomtech.mmtools.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;

import com.winomtech.mmtools.utils.Log;

/**
 * @author kevinhuang
 * @since 2015-01-20
 */
public class PcmRecorder {
	static final String TAG = PcmRecorder.class.getSimpleName();

	public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	public static final int AUDIO_FORMAT_IN_BYTE = 2;

	WavWriter 	mWavWriter;
	AudioRecord	mAudioRecord;
	boolean		mStopFlag = false;
	int			mBufSize;

	RecordThread	mRecordThread;

	public PcmRecorder(int sampleRate, int channelCnt, String filePath) {
		int channelConfig = channelCnt == 1 ? AudioFormat.CHANNEL_CONFIGURATION_MONO : AudioFormat.CHANNEL_CONFIGURATION_STEREO;
		int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, AUDIO_FORMAT);
		mBufSize = sampleRate * 20 / 1000 * channelCnt * AUDIO_FORMAT_IN_BYTE;
		mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, AUDIO_FORMAT, minBufSize);
		mWavWriter = new WavWriter(filePath, channelCnt, sampleRate, AUDIO_FORMAT);
	}

	public void startRecord() {
		Log.d(TAG, "startRecord");
		mRecordThread = new RecordThread();
		mRecordThread.start();
		
	}

	public void stopRecord() {
		Log.d(TAG, "stopRecord");
		mStopFlag = true;
		try {
			mRecordThread.join();
		} catch (InterruptedException e) {
			Log.e(TAG, "InterruptedException " + e.getMessage());
		}
	}

	class RecordThread extends Thread {
		@Override
		public void run() {
			Log.d(TAG, "thread run");
			Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

			byte[] buffer = new byte[mBufSize];
			mAudioRecord.startRecording();
			while (!mStopFlag) {
				int len = mAudioRecord.read(buffer, 0, buffer.length);
				Log.d(TAG, "record len: " + len);
				mWavWriter.writeToFile(buffer, len);
			}
			mWavWriter.closeFile();
			mAudioRecord.stop();
			mAudioRecord.release();
			Log.d(TAG, "thread end");
		}
	}
}
