package com.winomtech.androidmisc.plugin.camera.camera;

import android.app.Activity;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.view.Surface;

import com.winom.olog.Log;
import com.winomtech.androidmisc.common.utils.ApiLevel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author kevinhuang
 * @since 2015-04-01
 */
public class CameraV1Controller {
    final static String TAG = "CameraLoader";

    // 分辨率系数，选取摄像头预览和图片大小的时候，需要与预期值进行比例和差距加权求出差异值，然后取差异最小的
    final static double COEFFICIENT = 1000.0d;

    // 闪光灯的模式定义
    final static int MODE_OFF		= 0;	// 关闭闪光灯
    final static int MODE_AUTO		= 1;	// 闪关灯自动
    final static int MODE_MANUAL	= 2;	// 对焦的时候，手动打开闪关灯

    Camera mCamera;

    // 摄像头支持的比例放大比例的列表，和camera一起被初始化，如果不支持缩放，则不允许设置放大比例
    List<Integer> mZoomRatios = null;
    float mZoomValue = 100f;

    Activity mActivity;
    boolean mFocusEnd;
    boolean mUseFrontFace;        // 当前是否是使用前置摄像头
    Point mPreviewSize;

    int mFlashMode = MODE_OFF;

    int mDisplayRotate;
    int mMaxWidth;
    int mMaxHeight;
    int mFrameRate;

    public CameraV1Controller(Activity activity, boolean useFrontFace, CameraConfig config) {
        mActivity = activity;
        mFocusEnd = true;

        mUseFrontFace = useFrontFace;
        mMaxWidth = config.getWidth();
        mMaxHeight = config.getHeight();
        mFrameRate = config.getFrameRate();
    }

    public void setZoom(float factor) {
        if (null == mZoomRatios || null == mCamera) {
            return;
        }

        mZoomValue *= factor;
        try {
            if (mZoomValue < mZoomRatios.get(0)) {
                mZoomValue = mZoomRatios.get(0);
            }

            if (mZoomValue > mZoomRatios.get(mZoomRatios.size() - 1)) {
                mZoomValue = mZoomRatios.get(mZoomRatios.size() - 1);
            }

            Camera.Parameters params = mCamera.getParameters();
            int zoomIndex = getNearestZoomIndex((int) (mZoomValue));
            if (params.getZoom() != zoomIndex) {
                params.setZoom(zoomIndex);
                mCamera.setParameters(params);
            }
        } catch (Exception e) {
            Log.e(TAG, "setZoom failed, " + e.getMessage());
        }
    }

    int getNearestZoomIndex(int prefectVal) {
        int left = 0, right = mZoomRatios.size() - 1, middle;
        while (right - left > 1) {
            middle = (left + right) / 2;
            if (prefectVal > mZoomRatios.get(middle)) {
                left = middle;
            } else {
                right = middle;
            }
        }

        if (Math.abs(prefectVal - mZoomRatios.get(left)) > Math.abs(prefectVal - mZoomRatios.get(right))) {
            return right;
        } else {
            return left;
        }
    }

    Camera safeOpenCamera(boolean useFrontFace) {
        Log.i(TAG, "useFrontFace: " + useFrontFace);
        Camera camera = openCameraByHighApiLvl(useFrontFace);
        if (null == camera) {
            try {
                camera = Camera.open();
            } catch (Exception e) {
                Log.e(TAG, "openCameraFailed, " + e.getMessage());
            }
        }
        return camera;
    }

    Camera openCameraByHighApiLvl(boolean useFrontFace) {
        if (CameraCompat.gCameraInfo.getCameraNum() <= 0) {
            Log.i(TAG, "CameraNum is 0");
            return null;
        }

        Camera camera = null;
        try {
            if (true == useFrontFace) {
                camera = Camera.open(CameraCompat.gCameraInfo.getFrontId());
            } else {
                camera = Camera.open(CameraCompat.gCameraInfo.getBackId());
            }
        } catch (Exception e) {
            Log.e(TAG, "openCamera by high api level failed, " + e.getMessage());
        }

        return camera;
    }

    void safeSetPreviewFrameRate(Camera camera) {
        Camera.Parameters params = camera.getParameters();
        int fitRate = -1;

        List<Integer> rateList = params.getSupportedPreviewFrameRates();
        if (null == rateList || 0 == rateList.size()) {
            Log.e(TAG, "getSupportedPrviewFrameRates failed");
            return;
        }

        for (Integer rate : rateList) {
            Log.d(TAG, "supportPriviewFrameRate, rate: " + rate);
            if (rate <= mFrameRate && (-1 == fitRate || rate > fitRate)) {
                fitRate = rate;
            }
        }

        if (-1 == fitRate) {
            Log.e(TAG, "can't find fit rate, use camera default value");
            return;
        }

        try {
            Log.i(TAG, "setPreviewFrameRate, fitRate: " + fitRate);
            //noinspection deprecation
            params.setPreviewFrameRate(fitRate);
            camera.setParameters(params);
        } catch (Exception e) {
            Log.e(TAG, "setPreviewFrameRate failed, " + e.getMessage());
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    boolean safeSetPreviewSize(Camera camera) {
        Camera.Parameters params = camera.getParameters();
        Point size = null;

        List<Camera.Size> sizeLst = params.getSupportedPreviewSizes();
        if (null == sizeLst || 0 == sizeLst.size()) {
            Log.e(TAG, "getSupportedPrviewSizes failed");
            return false;
        }

        int diff = Integer.MAX_VALUE;
        for (Camera.Size it : sizeLst) {
            int width = it.width;
            int height = it.height;
            if (mDisplayRotate == 90 || mDisplayRotate == 270) {
                height = it.width;
                width = it.height;
            }

            Log.i(TAG, "supportPreview, width: %d, height: %d", width, height);
            if (width * height <= mMaxHeight * mMaxWidth) {
                int newDiff = diff(height, width, mMaxHeight, mMaxWidth);
                Log.d(TAG, "diff: " + newDiff);
                if (null == size || newDiff < diff) {
                    size = new Point(it.width, it.height);
                    diff = newDiff;
                }
            }
        }

        if (null == size) {
            Collections.sort(sizeLst, new Comparator<Camera.Size>() {
                @Override
                public int compare(Camera.Size lhs, Camera.Size rhs) {
                    return lhs.width * lhs.height - rhs.width * rhs.height;
                }
            });

            Camera.Size it = sizeLst.get(sizeLst.size() / 2);
            size = new Point(it.width, it.height);
        }

        try {
            Log.i(TAG, "setPreviewSize, width: %d, height: %d", size.x, size.y);
            params.setPreviewSize(size.x, size.y);
            if (mDisplayRotate == 90 || mDisplayRotate == 270) {
                mPreviewSize = new Point(size.y, size.x);
            } else {
                mPreviewSize = new Point(size.x, size.y);
            }
            camera.setParameters(params);
        } catch (Exception e) {
            Log.e(TAG, "setPreviewSize failed, " + e.getMessage());
            return false;
        }
        return true;
    }

    int diff(double realH, double realW, double expH, double expW) {
        double rateDiff = Math.abs(COEFFICIENT * (realH / realW - expH / expW));
        return (int) (rateDiff + Math.abs(realH - expH) + Math.abs(realW - expW));
    }

    void initRotateDegree(int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        Log.d(TAG, "cameraId: %d, roation: %d", cameraId, info.orientation);
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        mDisplayRotate = (info.orientation - degrees + 360) % 360;
    }

    Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            mFocusEnd = true;
            if (success) {
                if (MODE_MANUAL == mFlashMode) {
                    switchLight(false);
                }
            }
        }
    };

    public boolean initCamera() {
        return initCamera(mUseFrontFace);
    }

    public boolean switchCamera() {
        releaseCamera();
        mUseFrontFace = !mUseFrontFace;
        return initCamera(mUseFrontFace);
    }

    public boolean isUseFrontFace() {
        return mUseFrontFace;
    }

    public void switchAutoFlash(boolean open) {
        if (null == mCamera) {
            return;
        }

        Log.i(TAG, "switch auto flash: " + open);
        try {
            Camera.Parameters params = mCamera.getParameters();
            if (open) {
                // android L上面有BUG会导致开了auto之后，无法再off。
                if (Build.VERSION.SDK_INT < ApiLevel.API21_KLOLLIPOP &&
                        params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    mFlashMode = MODE_AUTO;
                } else {
                    mFlashMode = MODE_MANUAL;
                }
            } else {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mFlashMode = MODE_OFF;
            }
            Log.d(TAG, "flash mode: " + params.getFlashMode());
            mCamera.setParameters(params);
        } catch (Exception e) {
            Log.e(TAG, "can't set flash mode");
        }
    }

    public void switchLight(boolean open) {
        if (null == mCamera) {
            return;
        }

        try {
            Camera.Parameters params = mCamera.getParameters();
            if (open) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            } else {
                params.setFlashMode(MODE_MANUAL == mFlashMode ? Camera.Parameters.FLASH_MODE_OFF :
                        Camera.Parameters.FLASH_MODE_AUTO);
            }
            mCamera.setParameters(params);
        } catch (Exception e) {
            Log.e(TAG, "light up failed, " + e.getMessage());
        }
    }

    boolean initCamera(boolean useFrontFace) {
        Log.d(TAG, "initCameraInGLThread");
        mCamera = safeOpenCamera(useFrontFace);
        if (null == mCamera) {
            Log.e(TAG, "open camera failed");
            return false;
        }

        int cameraId = useFrontFace ? CameraCompat.gCameraInfo.getFrontId() : CameraCompat.gCameraInfo.getBackId();
        initRotateDegree(cameraId);

        try {
            safeSetPreviewFrameRate(mCamera);

            // 设置预览图片大小
            if (!safeSetPreviewSize(mCamera)) {
                Log.e(TAG, "safeSetPreviewSize failed");
                return false;
            }

            Camera.Parameters parameters = mCamera.getParameters();
            mZoomRatios = null;
            if (parameters.isZoomSupported()) {
                mZoomRatios = parameters.getZoomRatios();
                Collections.sort(mZoomRatios);
                Log.d(TAG, "ratios: " + mZoomRatios);
                mZoomValue = 100f;
            } else {
                Log.e(TAG, "camera don't support zoom");
            }

            List<String> supportModes = parameters.getSupportedFocusModes();
            if (supportModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else if (supportModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (supportModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            Log.i(TAG, "focusMode: " + parameters.getFocusMode());
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            releaseCamera();
            Log.e(TAG, "setParametersError false");
            return false;
        }

        return true;
    }

    public void releaseCamera() {
        if (null != mCamera) {
            try {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
            } catch (Exception e) {
                Log.e(TAG, "exception on releaseCameraInGLThread, " + e.getMessage());
            }
        }
        mCamera = null;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public int getDisplayRotate() {
        return mDisplayRotate;
    }

    // 获取当前期望的帧率,设置给摄像头的有可能没生效,所以外围再做处理
    public int getCameraFrameRate() {
        return mFrameRate;
    }
}
