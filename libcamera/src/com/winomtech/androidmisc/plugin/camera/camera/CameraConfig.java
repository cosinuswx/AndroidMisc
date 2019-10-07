package com.winomtech.androidmisc.plugin.camera.camera;

/**
 * @author kevinhuang
  */
public enum CameraConfig {
    FullScreen(720, 1280, 30),
    WideScreen(960, 1280, 30),
    FullScreenForLowPhone(480, 864, 15),
    WideScreenForLowPhone(600, 800, 15),
    VoIP(720, 1280, 15),
    VoIPForLowPhone(480, 864, 15);

    private int width;
    private int height;
    private int fps;

    CameraConfig(int width, int height, int fps) {
        this.width = width;
        this.height = height;
        this.fps = fps;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getFrameRate() {
        return this.fps;
    }
}
