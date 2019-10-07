package com.winomtech.androidmisc.plugin.camera.camera;

/**
 * @author kevinhuang
  */
public interface CameraPreviewCallback {
    void onPreviewFrame(byte[] data, ICameraLoader cameraLoader);
}
