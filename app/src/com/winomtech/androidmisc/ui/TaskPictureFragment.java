package com.winomtech.androidmisc.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.winom.olog.OLog;
import com.winomtech.androidmisc.R;

import java.io.IOException;
import java.util.List;

import androidx.fragment.app.Fragment;

public class TaskPictureFragment extends Fragment implements SurfaceHolder.Callback {
    private final static String TAG = TaskPictureFragment.class.getSimpleName();
    private final static int MAX_PICTURE_SIZE = 1920 * 1080;
    private final static int MIN_FPS = 15;
    private final static int MAX_FPS = 30;

    private SurfaceView mSurfaceView;
    private Button mBtnTakePic;
    private Camera mCamera;
    private ImageView mImageView;
    private Handler mUiHandler;
    private int mRotateDegree = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_take_picture, container, false);
        mSurfaceView = rootView.findViewById(R.id.sv_preview);
        mImageView = rootView.findViewById(R.id.iv_prview);
        mBtnTakePic = rootView.findViewById(R.id.btn_capture);
        mBtnTakePic.setOnClickListener(v -> mCamera.takePicture(null, null, mTakePictureCb));
        mUiHandler = new Handler();
        return rootView;
    }

    private void openAndInitCamera() {
        if (null != mCamera) {
            return;
        }

        try {
            int cameraId = getOneAvailableCameraId();
            mCamera = Camera.open(cameraId);
            mCamera.setPreviewDisplay(mSurfaceView.getHolder());
            mSurfaceView.getHolder().addCallback(this);
            setDisplayOrientation(mCamera, cameraId);

            Camera.Parameters params = mCamera.getParameters();
            setPrviewFrameRate(params);
            setPreviewSize(mCamera, params);
            setFitPictureSize(mCamera, params);
            mCamera.setParameters(params);
            mCamera.startPreview();
        } catch (IOException e) {
            OLog.e(TAG, "open failed: " + e.getMessage());
        }
    }

    private void releaseCamera() {
        if (null != mCamera) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void setPrviewFrameRate(Parameters params) {
        List<int[]> lstRanges = params.getSupportedPreviewFpsRange();
        int[] maxRange = new int[]{lstRanges.get(0)[Parameters.PREVIEW_FPS_MIN_INDEX],
                lstRanges.get(0)[Parameters.PREVIEW_FPS_MAX_INDEX]};
        for (int[] range : lstRanges) {
            int min = range[Parameters.PREVIEW_FPS_MAX_INDEX];
            int max = range[Parameters.PREVIEW_FPS_MIN_INDEX];
            if (min >= MIN_FPS && max <= MAX_FPS && max - min > maxRange[1] - maxRange[0]) {
                maxRange[0] = range[Parameters.PREVIEW_FPS_MIN_INDEX];
                maxRange[1] = range[Parameters.PREVIEW_FPS_MAX_INDEX];
            }
        }
        params.setPreviewFpsRange(maxRange[0], maxRange[1]);
    }

    private void setFitPictureSize(Camera camera, Parameters params) {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels - getActivity().getWindow().getDecorView().getHeight();

        List<Size> lstSize = params.getSupportedPictureSizes();
        Size maxSize = camera.new Size(0, 1);
        for (Size size : lstSize) {
            OLog.d(TAG, "picture size, width: %d, height: %d", size.width, size.height);
            if (size.width * size.height <= MAX_PICTURE_SIZE && maxSize.width * maxSize.height < size.width * size.height) {
                float curDiff = diff(size.width, size.height, width, height);
                float maxDiff = diff(maxSize.width, maxSize.height, width, height);
                if (curDiff < maxDiff) {
                    maxSize.width = size.width;
                    maxSize.height = size.height;
                }
            }
        }
        params.setPictureSize(maxSize.width, maxSize.height);
        OLog.d(TAG, "setFitPictureSize, width: %d, height: %d", maxSize.width, maxSize.height);
    }

    private float diff(float w1, float h1, float w2, float h2) {
        return Math.abs(h1 / w1 - h2 / w2);
    }

    private void setDisplayOrientation(Camera camera, int cameraId) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
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
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + 180 - degrees + 360) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
        mRotateDegree = (info.orientation - degrees + 360) % 360;
    }

    private int getOneAvailableCameraId() {
        int cameraCnt = Camera.getNumberOfCameras();
        int cameraId = 0;
        for (int i = 0; i < cameraCnt; ++i) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    private void setPreviewSize(Camera camera, Camera.Parameters params) {
        List<Size> lstPreview = camera.getParameters().getSupportedPreviewSizes();
        Size maxSize = camera.new Size(1, 1);
        for (Camera.Size size : lstPreview) {
            OLog.d(TAG, "width: %d, height: %d", size.width, size.height);
            if (maxSize.width * maxSize.height < size.width * size.height) {
                maxSize.width = size.width;
                maxSize.height = size.height;
            }
        }
        params.setPreviewSize(maxSize.width, maxSize.height);
    }

    private Camera.PictureCallback mTakePictureCb = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            matrix.setRotate(mRotateDegree);
            final Bitmap finBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            mUiHandler.post(() -> {
                mImageView.setImageBitmap(finBitmap);
                mImageView.setVisibility(View.VISIBLE);
                mSurfaceView.setVisibility(View.GONE);
                mBtnTakePic.setVisibility(View.GONE);
            });
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void onResume() {
        super.onResume();
        openAndInitCamera();
    }

    @Override
    public void onPause() {
        releaseCamera();
        super.onPause();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            mCamera.setPreviewDisplay(mSurfaceView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
}
