package com.winomtech.androidmisc.plugin.camera.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.winom.olog.Log;
import com.winomtech.androidmisc.plugin.camera.R;
import com.winomtech.androidmisc.plugin.camera.camera.CameraConfig;
import com.winomtech.androidmisc.plugin.camera.camera.CameraV1Loader;
import com.winomtech.androidmisc.plugin.camera.camera.CameraV2Loader;
import com.winomtech.androidmisc.plugin.camera.camera.ICameraLoader;
import com.winomtech.androidmisc.plugin.camera.draw.GPUImageView;
import com.winomtech.androidmisc.plugin.camera.draw.OnSurfaceListener;
import com.winomtech.androidmisc.plugin.camera.filter.BlackWhiteFilter;
import com.winomtech.androidmisc.plugin.camera.filter.GPUImageFilter;
import com.winomtech.androidmisc.plugin.camera.filter.GPUImageFilterGroup;
import com.winomtech.androidmisc.plugin.camera.filter.GPUImageFilterGroupBase;

/**
 * @author kevinhuang
 * @since 2017-03-02
 */
public abstract class CameraFragmentBase extends Fragment implements GPUImageView.OnGestureListener {
    private static final String TAG = "CameraFragment";

    private ViewGroup mRootView;
    private RelativeLayout mRlGPUImageViewCtn;
    private Button mBtnLight;

    private GPUImageView mGPUImageView;
    private ICameraLoader mCameraLoader;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.layout_camera_fragment, container, false);
        mRlGPUImageViewCtn = (RelativeLayout) mRootView.findViewById(R.id.rl_activity_gpuimage_container);

        mBtnLight = (Button) mRootView.findViewById(R.id.btn_light);
        mBtnLight.setOnClickListener(mLightListener);

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initGPUImageView();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeGPUImageView();
    }

    protected abstract boolean useCameraV2();

    private void initGPUImageView() {
        if (null != mGPUImageView) {
            return;
        }

        // GPUImage是先做的翻转，再做的旋转
        mGPUImageView = new GPUImageView(getActivity(), mSurfaceListener);
        mGPUImageView.setOnGestureListener(this);
        mRlGPUImageViewCtn.addView(mGPUImageView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    View.OnClickListener mLightListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            boolean lightOn = (null != v.getTag()) && ((boolean) v.getTag());
            lightOn = !lightOn;
            if (null != mCameraLoader) {
                mCameraLoader.switchLight(lightOn);
                v.setTag(lightOn);
            }
        }
    };

    OnSurfaceListener mSurfaceListener = new OnSurfaceListener() {

        @Override
        public void onSurfaceCreated() {
            Log.i(TAG, "init camera");

            CameraConfig exceptConfig = CameraConfig.FullScreen;
            if (useCameraV2()) {
                mCameraLoader = new CameraV2Loader(getActivity(), false, exceptConfig);
            } else {
                mCameraLoader = new CameraV1Loader(getActivity(), false, exceptConfig);
            }
            boolean ret = mCameraLoader.initCameraInGLThread();

            if (!ret) {
                Log.e(TAG, "initCameraInGLThread failed");
                return;
            }
            Log.i(TAG, "initCameraInGLThread succeed");


            mGPUImageView.getGPUImage().setUpCamera(mCameraLoader, mCameraLoader.getCameraFrameRate(),
                    mCameraLoader.getDisplayRotate(), mCameraLoader.isUseFrontFace(), false);

            GPUImageFilterGroupBase filterGroup = new GPUImageFilterGroup();
            filterGroup.addFilter(new GPUImageFilter());
            mGPUImageView.setFilter(filterGroup);
        }

        @Override
        public void onSurfaceDestroyed() {
            if (null != mCameraLoader) {
                mCameraLoader.releaseCameraInGLThread();
            }
            mCameraLoader = null;
        }
    };

    protected void removeGPUImageView() {
        if (null != mGPUImageView) {
            mGPUImageView.onPause();
            mRlGPUImageViewCtn.removeView(mGPUImageView);
            mGPUImageView.uninit();
            mGPUImageView = null;
        }
    }

    @Override
    public boolean onSingleTap(MotionEvent e) {
        if (null == mGPUImageView) {
            return false;
        }

        mCameraLoader.focusOnTouch(e, mGPUImageView.getWidth(), mGPUImageView.getHeight());
        return true;
    }

    @Override
    public void onDoubleTap() {
    }

    @Override
    public void onScale(float factor) {
    }

    @Override
    public void showPress() {
    }

    @Override
    public void onLongPress() {
    }

    @Override
    public void onActionUp() {
    }
}
