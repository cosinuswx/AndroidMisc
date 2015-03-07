package com.winomtech.androidmisc.sdk.utils;

import android.os.Handler;
import android.os.Message;

public class SmTimer extends Handler {

	private int		mInterval;
	private boolean	mLoop = false;

	private SmTimerCallback		mCallback;

	public interface SmTimerCallback {
		public void		onTimeout();
	}

	public SmTimer(SmTimerCallback callback) {
		mCallback = callback;
	}

	@Override
	public void handleMessage(Message msg) {
		if (null != mCallback) {
			mCallback.onTimeout();
		}

		if (mLoop) {
			stopTimer();
			sendEmptyMessageDelayed(0, mInterval);
		}
	}

	public void startIntervalTimer(int delay, int interval) {
		stopTimer();

		mInterval = interval;
		mLoop = true;
		sendEmptyMessageDelayed(0, delay);
	}

	public void stopTimer() {
		while (hasMessages(0)) {
			removeMessages(0);
		}
	}
}
