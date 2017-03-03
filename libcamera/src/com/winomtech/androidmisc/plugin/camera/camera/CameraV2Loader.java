package com.winomtech.androidmisc.plugin.camera.camera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.view.Surface;

import com.winom.olog.Log;
import com.winomtech.androidmisc.common.utils.ApiLevel;
import com.winomtech.androidmisc.common.utils.Size;
import com.winomtech.androidmisc.plugin.jni.JniEntry;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author kevinhuang
 * @since 2017-03-02
 */
@TargetApi(ApiLevel.API21_KLOLLIPOP)
public class CameraV2Loader implements ICameraLoader, ImageReader.OnImageAvailableListener {
    private static final String TAG = "CameraV2Loader";

    private Activity mActivity;
    private boolean mUseFrontFace;
    private int mMaxWidth;
    private int mMaxHeight;
    private int mFrameRate;
    private CameraPreviewCallback mPreviewCallback;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest mPreviewRequest;

    private boolean mFlashSupported;
    private int mFlashMode = MODE_MANUAL;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private ImageReader mImageReader;

    private int mDisplayRotate;
    private Size mPreviewSize;
    private ByteBuffer mPreviewBuf;
    private boolean mBufInUsing;

    public CameraV2Loader(Activity activity, boolean useFrontFace, CameraConfig config) {
        mActivity = activity;
        mUseFrontFace = useFrontFace;

        mMaxWidth = config.getWidth();
        mMaxHeight = config.getHeight();
        mFrameRate = config.getFrameRate();
    }

    @Override
    public boolean initCameraInGLThread() {
        startBackgroundThread();
        return openCamera();
    }

    @Override
    public boolean switchCameraInGLThread() {
        closeCamera();
        mUseFrontFace = !mUseFrontFace;
        return openCamera();
    }

    @Override
    public boolean isUseFrontFace() {
        return mUseFrontFace;
    }

    @Override
    public void switchAutoFlash(boolean open) {
        if (!mFlashSupported || null == mCameraDevice) {
            return;
        }

        // Flash is automatically enabled when necessary.
        mFlashMode = open ? MODE_AUTO : MODE_MANUAL;
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE,
                open ? CaptureRequest.CONTROL_MODE_AUTO : CaptureRequest.CONTROL_MODE_OFF);
    }

    @Override
    public void switchLight(boolean open) {
        if (!mFlashSupported || null == mCameraDevice) {
            return;
        }

        if (open) {
            mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
        } else {
            if (MODE_AUTO == mFlashMode) {
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            } else {
                mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            }
        }

        mPreviewRequest = mPreviewRequestBuilder.build();
        try {
            mCaptureSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setZoom(float factor) {
    }

    @Override
    public void releaseCameraInGLThread() {
        closeCamera();
        stopBackgroundThread();
    }

    @Override
    public void addCallbackBuffer(byte[] data) {
        mBufInUsing = false;
    }

    @Override
    public int getCameraFrameRate() {
        return 30;
    }

    @Override
    public int getDisplayRotate() {
        return mDisplayRotate;
    }

    @Override
    public void setPreviewCallback(CameraPreviewCallback callback) {
        mPreviewCallback = callback;
    }

    @Override
    public Size getPreviewSize() {
        return mPreviewSize;
    }

    private boolean openCamera() {
        CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        try {
            String chooseCameraId = null;
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                // 不是指定的摄像头，则直接跳过
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing == null
                        || (mUseFrontFace && facing == CameraCharacteristics.LENS_FACING_BACK)
                        || (!mUseFrontFace && facing == CameraCharacteristics.LENS_FACING_FRONT)) {
                    continue;
                }

                initRotateDegree(characteristics);
                mPreviewSize = chooseBestPreviewSize(characteristics);
                if (null == mPreviewSize) {
                    continue;
                }

                mImageReader = ImageReader.newInstance(mPreviewSize.width, mPreviewSize.height, ImageFormat.YUV_420_888, 2);
                mImageReader.setOnImageAvailableListener(this, mBackgroundHandler);

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                chooseCameraId = cameraId;
                break;
            }

            if (null == chooseCameraId) {
                return false;
            }

            if (!mCameraOpenCloseLock.tryAcquire(25000, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(chooseCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "open camera failed, errMsg: " + e.getMessage());
            return false;
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException on openCamera, errMsg: " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Log.e(TAG, "open camera been interrupt, errMsg: " + e.getMessage());
            return false;
        }
        
        return true;
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            mCameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "camera device error: " + error);
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
        }
    };

    CameraCaptureSession.StateCallback mCreateSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            // The camera is already closed
            if (null == mCameraDevice) {
                return;
            }

            mCaptureSession = session;
            // Auto focus should be continuous for camera preview.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // Finally, we start displaying the camera preview.
            mPreviewRequest = mPreviewRequestBuilder.build();
            try {
                mCaptureSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "configure camera failed");
        }
    };

    private void createCameraPreviewSession() {
        try {
            mPreviewBuf = ByteBuffer.allocateDirect(mPreviewSize.width * mPreviewSize.height * 3 / 2);
            mBufInUsing = false;

            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mImageReader.getSurface());
            mCameraDevice.createCaptureSession(Collections.singletonList(mImageReader.getSurface()), mCreateSessionCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "create camera preview failed, errMsg: " + e.getMessage());
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initRotateDegree(CameraCharacteristics characteristics) {
        int displayRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (displayRotation) {
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

        //noinspection ConstantConditions
        int senseOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        mDisplayRotate = (senseOrientation - degrees + 360) % 360;
    }

    private Size chooseBestPreviewSize(CameraCharacteristics characteristics) {
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
            Log.e(TAG, "can't get data from CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP");
            return null;
        }

        android.util.Size[] sizes = map.getOutputSizes(ImageFormat.YUV_420_888);
        if (null == sizes || 0 == sizes.length) {
            Log.e(TAG, "no output size for NV21");
            return null;
        }

        Size choosedSize = null;
        int diff = Integer.MAX_VALUE;

        for (int i = 0; i < sizes.length; ++i) {
            android.util.Size it = sizes[i];
            int width = it.getWidth();
            int height = it.getHeight();
            if (mDisplayRotate == 90 || mDisplayRotate == 270) {
                height = it.getWidth();
                width = it.getHeight();
            }

            Log.i(TAG, "supportPreview, width: %d, height: %d", width, height);
            if (width * height <= mMaxHeight * mMaxWidth) {
                int newDiff = diff(height, width, mMaxHeight, mMaxWidth);
                Log.d(TAG, "diff: " + newDiff);
                if (null == choosedSize || newDiff < diff) {
                    choosedSize = new Size(it.getWidth(), it.getHeight());
                    diff = newDiff;
                }
            }
        }

        if (null == choosedSize) {
            List<android.util.Size> sizeLst = Arrays.asList(sizes);
            Collections.sort(sizeLst, new Comparator<android.util.Size>() {
                @Override
                public int compare(android.util.Size lhs, android.util.Size rhs) {
                    return lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight();
                }
            });

            android.util.Size it = sizeLst.get(sizeLst.size() / 2);
            choosedSize = new Size(it.getWidth(), it.getHeight());
        }
        
        return choosedSize;
    }

    private int diff(double realH, double realW, double expH, double expW) {
        double rateDiff = Math.abs(COEFFICIENT * (realH / realW - expH / expW));
        return (int) (rateDiff + Math.abs(realH - expH) + Math.abs(realW - expW));
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        if (mBufInUsing) {
            return;
        }

        Image image = reader.acquireLatestImage();
        if (null == image) {
            return;
        }

        Image.Plane[] planes = image.getPlanes();
        if (null == planes || planes.length != 3) {
            image.close();
            return;
        }

        int area = mPreviewSize.width * mPreviewSize.height;
        JniEntry.CopyData(planes[0].getBuffer(), 0, mPreviewBuf, 0, area);
        JniEntry.mixUV(mPreviewBuf, area, planes[2].getBuffer(), planes[1].getBuffer(), planes[1].getPixelStride(), area / 4);

        image.close();

        if (null != mPreviewCallback) {
            mBufInUsing = true;
            mPreviewCallback.onPreviewFrame(mPreviewBuf.array(), this);
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }
}
