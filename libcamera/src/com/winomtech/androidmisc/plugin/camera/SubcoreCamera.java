package com.winomtech.androidmisc.plugin.camera;

import android.renderscript.RenderScript;

import com.winom.olog.OLog;
import com.winomtech.androidmisc.common.constants.Constants;
import com.winomtech.androidmisc.common.cores.AmCore;
import com.winomtech.androidmisc.common.cores.ISubCore;
import com.winomtech.androidmisc.plugin.camera.camera.CameraCompat;

/**
 * @author kevinhuang
  */
public class SubcoreCamera implements ISubCore {
    private static final String TAG = "SubcoreCamera";

    private static SubcoreCamera thiz = null;

    public static SubcoreCamera getCore() {
        if (null == thiz) {
            thiz = new SubcoreCamera();
            AmCore.getCore().addSubCore(Constants.SUBCORE_CAMERA, thiz);
        }
        return thiz;
    }


    private RenderScript mRenderScript;
    private boolean mCanUseRs;

    public SubcoreCamera() {
        try {
            mRenderScript = RenderScript.create(AmCore.getCore().getContext());
            mCanUseRs = true;
            OLog.i(TAG, "can use renderscript");
        } catch (Exception e) {
            mCanUseRs = false;
            OLog.i(TAG, "can't use renderscript");
        }

        CameraCompat.initCameraInfo();
    }

    @Override
    public void onAccountPostSet() {
    }

    @Override
    public void onAccountPreRelease() {
    }

    public RenderScript getGlobalRs() {
        return mRenderScript;
    }

    public boolean canUseRs() {
        return mCanUseRs;
    }
}
