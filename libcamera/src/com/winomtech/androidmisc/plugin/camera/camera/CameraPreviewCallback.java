package com.winomtech.androidmisc.plugin.camera.camera;

/**
 * @author kevinhuang
 * @since 2017-03-02
 */
public interface CameraPreviewCallback {
    void onPreviewFrame(byte[] data, ICameraLoader cameraLoader);
}
