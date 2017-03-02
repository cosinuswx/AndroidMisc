package com.winomtech.androidmisc.plugin.camera.filter;

/**
 * @since 2016-07-26
 * @author kevinhuang 
 */
public class FilterConstants {
    /**
     * 最多支持的人数,所有特效的最多人数都不会超过这个
     */
    public static final int MAX_FACE_COUNT = 5;

    /**
     * 绘制摄像头里面的照片的矩阵
     */
    public static final float[] CUBE = {-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f};

    /**
     * 调试OpenGL错误
     */
    public static boolean DEBUG_GL_ERROR = false;
}
