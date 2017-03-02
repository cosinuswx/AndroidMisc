package com.winomtech.androidmisc.plugin.camera;

import android.renderscript.RenderScript;

import com.winomtech.androidmisc.plugin.camera.draw.CameraCompat;
import com.winom.olog.Log;
import com.winomtech.androidmisc.common.constants.Constants;
import com.winomtech.androidmisc.common.cores.AmCore;
import com.winomtech.androidmisc.common.cores.ISubCore;

/**
 * @author kevinhuang
 * @since 2016-12-15
 */
public class SubcoreCamera implements ISubCore {
    static final String TAG = "SubcoreCamera";

    static SubcoreCamera thiz = null;

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
            Log.i(TAG, "can use renderscript");
        } catch (Exception e) {
            mCanUseRs = false;
            Log.i(TAG, "can't use renderscript");
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
