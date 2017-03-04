package com.winomtech.androidmisc.plugin.camera.camera;

import android.view.MotionEvent;

import com.winomtech.androidmisc.common.utils.Size;

/**
 * @author kevinhuang
 * @since 2017-03-02
 */
public interface ICameraLoader {
    // 分辨率系数，选取摄像头预览和图片大小的时候，需要与预期值进行比例和差距加权求出差异值，然后取差异最小的
    double COEFFICIENT = 1000.0d;

    // 闪光灯的模式定义
    int MODE_OFF    = 0;    // 关闭闪光灯
    int MODE_AUTO   = 1;    // 闪关灯自动
    int MODE_MANUAL = 2;    // 对焦的时候，手动打开闪关灯，比如对焦的时候需要打开闪光灯


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
     * 设置缩放比例，里面会按照当前的比例再去缩放
     * @param factor 缩放比例
     */
    void setZoom(float factor);

    /**
     * 在对应的位置触发自动对焦
     * @param event 点击的事件
     * @param viewWidth 事件对应的view的宽度
     * @param viewHeight 事件对应的view的高度
     */
    void focusOnTouch(final MotionEvent event, final int viewWidth, final int viewHeight);

    /**
     * 关闭摄像头
     */
    void releaseCameraInGLThread();

    /**
     * 将在{@link CameraPreviewCallback#onPreviewFrame(byte[], ICameraLoader)}回调时的数据参数，
     * 返回到CameraLoader中重复利用。
     * @param data 之前返回的数据
     */
    void addCallbackBuffer(byte[] data);

    /**
     * 返回当前摄像头设置的帧率
     */
    int getCameraFrameRate();

    /**
     * 返回当前显示的旋转角度（根据摄像头角度和屏幕角度计算出来的角度）
     */
    int getDisplayRotate();

    /**
     * 设置数据回调的监听
     * @param callback 数据回调的监听
     */
    void setPreviewCallback(CameraPreviewCallback callback);

    /**
     * 返回预览尺寸
     */
    Size getPreviewSize();
}
