package com.winomtech.androidmisc.plugin.camera.camera;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;

import com.winom.olog.Log;

import java.nio.ByteBuffer;

/**
 * @author kevinhuang
 * @since 2017-03-02
 */
public class CameraV1Loader implements ICameraLoader, Camera.PreviewCallback {
    private static final String TAG = "CameraV1Loader";

    CameraV1Controller mCameraController;
    SurfaceTexture mSurfaceTexture = null;
    ByteBuffer mPreviewBuf;

    CameraPreviewCallback mPreviewCallback;

    public CameraV1Loader(Activity activity, boolean useFrontFace, CameraConfig config) {
        mCameraController = new CameraV1Controller(activity, useFrontFace, config);
    }

    public void setPreviewCallback(CameraPreviewCallback callback) {
        mPreviewCallback = callback;
    }

    @Override
    public boolean initCameraInGLThread() {
        if (!mCameraController.initCamera()) {
            return false;
        }

        setUpSurfaceTexture();
        return true;
    }

    private void setUpSurfaceTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mSurfaceTexture = new SurfaceTexture(textures[0]);

        Camera camera = mCameraController.getCamera();
        Camera.Size size = camera.getParameters().getPreviewSize();
        mPreviewBuf = ByteBuffer.allocateDirect(size.width * size.height * 3 / 2);

        try {
            camera.addCallbackBuffer(mPreviewBuf.array());
            camera.setPreviewTexture(mSurfaceTexture);
            camera.setPreviewCallbackWithBuffer(this);
            camera.startPreview();
        } catch (Exception e) {
            Log.e(TAG, "setup camera failed, " + e.getMessage());
        }
    }

    @Override
    public boolean switchCameraInGLThread() {
        if (!mCameraController.switchCamera()) {
            return false;
        }

        setUpSurfaceTexture();
        return true;
    }

    @Override
    public void releaseCameraInGLThread() {
        mCameraController.releaseCamera();
        mSurfaceTexture.release();
        mSurfaceTexture = null;
        mPreviewBuf = null;
    }

    @Override
    public void addCallbackBuffer(byte[] data) {
        mCameraController.getCamera().addCallbackBuffer(data);
    }

    @Override
    public boolean isUseFrontFace() {
        return mCameraController.isUseFrontFace();
    }


    @Override
    public void setZoom(float factor) {
        mCameraController.setZoom(factor);
    }

    @Override
    public void switchAutoFlash(boolean open) {
        mCameraController.switchAutoFlash(open);
    }

    @Override
    public void switchLight(boolean open) {
        mCameraController.switchLight(open);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (null != mPreviewCallback) {
            mPreviewCallback.onPreviewFrame(data, this);
        }
    }
}
