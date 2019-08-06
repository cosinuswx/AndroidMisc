package com.winomtech.androidmisc.plugin.camera.camera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.Surface;

import com.winom.olog.OLog;
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
    private int mFlashMode = MODE_OFF;
    private Rect mActiveArraySize = new Rect(0, 0, 1, 1);

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
        mFlashMode = open ? MODE_AUTO : MODE_OFF;
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

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void focusOnTouch(MotionEvent event, int viewWidth, int viewHeight) {
        if (null == mCameraDevice || null == mCaptureSession || null == mPreviewRequest) {
            return;
        }

        // 先取相对于view上面的坐标
        double x = event.getX(), y = event.getY(), tmp;

        int realPreviewWidth = mPreviewSize.width, realPreviewHeight = mPreviewSize.height;
        if (90 == mDisplayRotate || 270 == mDisplayRotate) {
            realPreviewWidth = mPreviewSize.height;
            realPreviewHeight = mPreviewSize.width;
        }

        // 计算摄像头取出的图像相对于view放大了多少，以及有多少偏移
        double imgScale = 1.0, verticalOffset = 0, horizontalOffset = 0;
        if (realPreviewHeight * viewWidth > realPreviewWidth * viewHeight) {
            imgScale = viewWidth * 1.0 / realPreviewWidth;
            verticalOffset = (realPreviewHeight - viewHeight / imgScale) / 2;
        } else {
            imgScale = viewHeight * 1.0 / realPreviewHeight;
            horizontalOffset = (realPreviewWidth - viewWidth / imgScale) / 2;
        }

        // 将点击的坐标转换为图像上的坐标
        x = x / imgScale + horizontalOffset;
        y = y / imgScale + verticalOffset;
        if (90 == mDisplayRotate) {
            tmp = x; x = y; y = mPreviewSize.height - tmp;
        } else if (270 == mDisplayRotate) {
            tmp = x; x = mPreviewSize.width - y; y = tmp;
        }

        // 计算取到的图像相对于裁剪区域的缩放系数，以及位移
        Rect cropRegion = mPreviewRequest.get(CaptureRequest.SCALER_CROP_REGION);
        if (null == cropRegion) {
            OLog.e(TAG, "can't get crop region");
            cropRegion = mActiveArraySize;
        }

        int cropWidth = cropRegion.width(), cropHeight = cropRegion.height();
        if (mPreviewSize.height * cropWidth > mPreviewSize.width * cropHeight) {
            imgScale = cropHeight * 1.0 / mPreviewSize.height;
            verticalOffset = 0;
            horizontalOffset = (cropWidth - imgScale * mPreviewSize.width) / 2;
        } else {
            imgScale = cropWidth * 1.0 / mPreviewSize.width;
            horizontalOffset = 0;
            verticalOffset = (cropHeight - imgScale * mPreviewSize.height) / 2;
        }

        // 将点击区域相对于图像的坐标，转化为相对于成像区域的坐标
        x = x * imgScale + horizontalOffset + cropRegion.left;
        y = y * imgScale + verticalOffset + cropRegion.top;

        double tapAreaRatio = 0.1;
        Rect rect = new Rect();
        rect.left = clamp((int) (x - tapAreaRatio / 2 * cropRegion.width()), 0, cropRegion.width());
        rect.right = clamp((int) (x + tapAreaRatio / 2 * cropRegion.width()), 0, cropRegion.width());
        rect.top = clamp((int) (y - tapAreaRatio / 2 * cropRegion.height()), 0, cropRegion.height());
        rect.bottom = clamp((int) (y + tapAreaRatio / 2 * cropRegion.height()), 0, cropRegion.height());

        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[] {new MeteringRectangle(rect, 1000)});
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[] {new MeteringRectangle(rect, 1000)});
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);

        mPreviewRequest = mPreviewRequestBuilder.build();
        try {
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mAfCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            OLog.e(TAG, "setRepeatingRequest failed, " + e.getMessage());
        }
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

    private int clamp(int x, int min, int max) {
        if (x > max) return max;
        if (x < min) return min;
        return x;
    }

    private CameraCaptureSession.CaptureCallback mAfCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult result) {
            Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
            if (null == afState) {
                return;
            }

            if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                    CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                startNormalPreview();
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }
    };

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

                mActiveArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
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
            OLog.e(TAG, "open camera failed, errMsg: " + e.getMessage());
            return false;
        } catch (SecurityException e) {
            OLog.e(TAG, "SecurityException on openCamera, errMsg: " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            OLog.e(TAG, "open camera been interrupt, errMsg: " + e.getMessage());
            return false;
        }

        return true;
    }

    private void startNormalPreview() {
        // Auto focus should be continuous for camera preview.
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);

        mPreviewRequest = mPreviewRequestBuilder.build();
        try {
            mCaptureSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            OLog.e(TAG, "setRepeatingRequest failed, errMsg: " + e.getMessage());
        }
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
            OLog.e(TAG, "camera device error: " + error);
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
            startNormalPreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            OLog.e(TAG, "configure camera failed");
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
            OLog.e(TAG, "create camera preview failed, errMsg: " + e.getMessage());
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
            OLog.e(TAG, "can't get data from CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP");
            return null;
        }

        android.util.Size[] sizes = map.getOutputSizes(ImageFormat.YUV_420_888);
        if (null == sizes || 0 == sizes.length) {
            OLog.e(TAG, "no output size for NV21");
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

            OLog.i(TAG, "supportPreview, width: %d, height: %d", width, height);
            if (width * height <= mMaxHeight * mMaxWidth) {
                int newDiff = diff(height, width, mMaxHeight, mMaxWidth);
                OLog.d(TAG, "diff: " + newDiff);
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

        OLog.i(TAG, "setPreviewSize width: %d, height: %d", choosedSize.width, choosedSize.height);
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
        if (planes[0].getRowStride() == mPreviewSize.width) {
            JniEntry.CopyData(planes[0].getBuffer(), 0, mPreviewBuf, 0, area);
        } else {
            JniEntry.CopyImage(planes[0].getBuffer(), 0, mPreviewBuf, 0,
                    mPreviewSize.width, mPreviewSize.height,
                    planes[0].getPixelStride(), planes[0].getRowStride());
        }

        JniEntry.mixUV(mPreviewBuf, area, planes[2].getBuffer(), planes[1].getBuffer(),
                mPreviewSize.width, mPreviewSize.height, planes[1].getPixelStride(), planes[0].getRowStride());

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
