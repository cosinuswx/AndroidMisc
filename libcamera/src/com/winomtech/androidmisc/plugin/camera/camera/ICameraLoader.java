package com.winomtech.androidmisc.plugin.camera.camera;

/**
 * @author kevinhuang
 * @since 2017-03-02
 */
public interface ICameraLoader {
    /**
     * 设置缩放比例，里面会按照当前的比例再去缩放
     * @param factor 缩放比例
     */
    void setZoom(float factor);

    /**
     * 初始化摄像头
     * @return 返回摄像头初始化成功还是失败
     */
    boolean initCameraInGLThread();

    /**
     * 切换摄像头
     * @return 返回摄像头切换成功还是失败
     */
    boolean switchCameraInGLThread();

    /**
     * 是否当前是使用的前置摄像头
     * @return 如果当前使用的是前置摄像头，则返回true，否则返回false
     */
    boolean isUseFrontFace();

    /**
     * 打开或者关闭自动闪光灯
     * @param open 如果为true，则是打开自动闪光灯，反之则关闭
     */
    void switchAutoFlash(boolean open);

    /**
     * 打开或者关闭闪光灯
     * @param open 为true则为打开闪光灯，为false则为关闭闪光灯
     */
    void switchLight(boolean open);

    /**
     * 关闭摄像头
     */
    void releaseCameraInGLThread();

    void addCallbackBuffer(byte[] data);
}
