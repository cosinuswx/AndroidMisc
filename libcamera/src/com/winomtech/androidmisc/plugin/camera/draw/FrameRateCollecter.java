package com.winomtech.androidmisc.plugin.camera.draw;

import com.winom.olog.OLog;
import com.winomtech.androidmisc.common.constants.Constants;

public class FrameRateCollecter {
    private static final String TAG = "FrameRateCollecter";

    private String mName;
    private long mStartTick;
    private long mFrameCount;
    private long mLastFrameTick;
    private long mLastOutputTIck;

    public FrameRateCollecter(String name) {
        mName = name;
        reset();
    }

    private void reset() {
        mStartTick = -1;
        mFrameCount = 0;
        mLastFrameTick = -1;
        mLastOutputTIck = System.currentTimeMillis();
    }

    public void onFrameAvailable() {
        long tick = System.currentTimeMillis();

        // 如果上一帧离这一帧时间过长,则需要重置一次,否则平均帧率就会受影响
        if (-1 != mLastFrameTick && (tick - mLastFrameTick > Constants.MS_PER_SECOND)) {
            reset();
        }

        if (-1 == mStartTick) {
            mStartTick = tick;
        }

        mFrameCount ++;
        mLastFrameTick = tick;

        if (tick - mLastOutputTIck >= Constants.MS_PER_SECOND) {
            OLog.v(TAG, "name: %s fps: %.1f", mName, (mFrameCount * Constants.MS_PER_SECOND / (tick - mStartTick)));
            mLastOutputTIck = tick;
        }
    }
}
