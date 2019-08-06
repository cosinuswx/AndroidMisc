/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.winomtech.androidmisc.plugin.camera.draw;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.view.ViewTreeObserver;

import com.winom.olog.OLog;
import com.winomtech.androidmisc.plugin.camera.camera.ICameraLoader;
import com.winomtech.androidmisc.plugin.camera.filter.GPUImageFilter;
import com.winomtech.androidmisc.plugin.camera.filter.GPUImageFilterGroup;
import com.winomtech.androidmisc.plugin.camera.filter.GPUImageFilterGroupBase;
import com.winomtech.androidmisc.plugin.camera.utils.Rotation;

/**
 * The main accessor for GPUImage functionality. This class helps to do common
 * tasks through a simple interface.
 */
public class GPUImage {
    static final String TAG = "GPUImage";
    final static int DEFAULT_SURFACE_FIXED_WIDTH = 720;
    final static int DEFAULT_SURFACE_FIXED_HEIGHT = 1440;

    public final GPUImageRenderer mRenderer;
    private GLSurfaceView mGlSurfaceView;
    public Bitmap mCurrentBitmap;

    int mSurfaceFixedWidth = DEFAULT_SURFACE_FIXED_WIDTH;
    int mSurfaceFixedHeight = DEFAULT_SURFACE_FIXED_HEIGHT;

    /**
     * Instantiates a new GPUImage object.
     *
     * @param context the context
     */
    public GPUImage(final Context context, OnSurfaceListener listener) {
        if (!supportsOpenGLES2(context)) {
            throw new IllegalStateException("OpenGL ES 2.0 is not supported on this phone.");
        } 
        mCurrentBitmap = null;
        GPUImageFilterGroupBase groupBase = new GPUImageFilterGroup();
        groupBase.addFilter(new GPUImageFilter());
        mRenderer = new GPUImageRenderer(groupBase, listener);
    }

    public GPUImageRenderer getRenderer() {
        return mRenderer;
    }

    /**
     * Checks if OpenGL ES 2.0 is supported on the current device.
     *
     * @param context the context
     * @return true, if successful
     */
    private boolean supportsOpenGLES2(final Context context) {
        final ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }

    /**
     * Sets the GLSurfaceView which will display the preview.
     *
     * @param view the GLSurfaceView
     */
    public void setGLSurfaceView(final GLSurfaceView view) {
        mGlSurfaceView = view;
        mGlSurfaceView.setEGLContextClientVersion(2);
        mGlSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGlSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        mGlSurfaceView.setRenderer(mRenderer);
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGlSurfaceView.requestRender();

        mGlSurfaceView.getViewTreeObserver().addOnGlobalLayoutListener(new OnSurfaceViewLayoutLsn());
    }

    public void setMaxFixedSize(int maxWidth, int maxHeight) {
        mSurfaceFixedWidth = maxWidth;
        mSurfaceFixedHeight = maxHeight;
    }

    class OnSurfaceViewLayoutLsn implements ViewTreeObserver.OnGlobalLayoutListener {

        @Override
        public void onGlobalLayout() {
            mGlSurfaceView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

            // 如果超过720p的手机的话,使用fixSize提高性能
            if (mGlSurfaceView.getWidth() > mSurfaceFixedWidth || mGlSurfaceView.getHeight() > mSurfaceFixedHeight) {
                int width = mSurfaceFixedWidth;
                int height = mGlSurfaceView.getHeight() * width / mGlSurfaceView.getWidth();
                if (height > mSurfaceFixedHeight) {
                    height = mSurfaceFixedHeight;
                    width = mGlSurfaceView.getWidth() * height / mGlSurfaceView.getHeight();
                }

                OLog.i(TAG, "setFixedSize width: %d, height: %d", width, height);
                mGlSurfaceView.getHolder().setFixedSize(width, height);
            }
        }
    }

    /**
     * Request the preview to be rendered again.
     */
    public void requestRender() {
        if (mGlSurfaceView != null) {
            mGlSurfaceView.requestRender();
        }
    }

    /**
     * Sets the up camera to be connected to GPUImage to get a filtered preview.
     *
     * @param frameRate camera frame rate which we excepted
     * @param degrees by how many degrees the image should be rotated
     * @param flipHorizontal if the image should be flipped horizontally
     * @param flipVertical if the image should be flipped vertically
     */
    public void setUpCamera(final ICameraLoader cameraLoader,
                            int frameRate,
                            final int degrees,
                            final boolean flipHorizontal,
                            final boolean flipVertical) {
        if (null == cameraLoader) {
            OLog.e(TAG, "setUpCamera failed, camera is null");
            return;
        }

        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mRenderer.setGlSurfaceView(mGlSurfaceView);
        Rotation rotation = Rotation.NORMAL;
        switch (degrees) {
            case 90:
                rotation = Rotation.ROTATION_90;
                break;
            case 180:
                rotation = Rotation.ROTATION_180;
                break;
            case 270:
                rotation = Rotation.ROTATION_270;
                break;
        }
        mRenderer.setRotationCamera(rotation, flipHorizontal, flipVertical);
        mRenderer.setFrameRate(frameRate);

        cameraLoader.setPreviewCallback(mRenderer);
    }

    /**
     * Sets the filter which should be applied to the image which was (or will
     * be) set by setImage(...).
     *
     * @param filter the new filter
     */
    public void setFilter(final GPUImageFilterGroupBase filter) {
        mRenderer.setFilter(filter);
        requestRender();
    }

    /**
     * Sets the image on which the filter should be applied.
     *
     * @param bitmap the new image
     */
    public void setImage(final Bitmap bitmap) {
        mCurrentBitmap = bitmap;
        mRenderer.setImageBitmap(bitmap, false);
        requestRender();
    }

    /**
     * This sets the scale type of GPUImage. This has to be run before setting the image.
     * If image is set and scale type changed, image needs to be reset.
     *
     * @param scaleType The new ScaleType
     */
    public void setScaleType(ScaleType scaleType) {
        mRenderer.setScaleType(scaleType);
        mRenderer.deleteImage();
        mCurrentBitmap = null;
        requestRender();
    }

    public void uninit() {
        if (null != mRenderer) {
            mRenderer.uninit();
        }
    }

    /**
     * Runs the given Runnable on the OpenGL thread.
     *
     * @param runnable The runnable to be run on the OpenGL thread.
     */
    void runOnGLThread(Runnable runnable) {
        mRenderer.addRunnableOnDrawEnd(runnable);
    }

    public enum ScaleType { CENTER_INSIDE, CENTER_CROP }
}
