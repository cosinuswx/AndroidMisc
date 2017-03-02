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

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.FrameLayout;

import com.winomtech.androidmisc.plugin.camera.filter.GPUImageFilter;
import com.winomtech.androidmisc.plugin.camera.filter.GPUImageFilterGroupBase;
import com.winom.olog.Log;

import java.nio.IntBuffer;
import java.util.concurrent.Semaphore;

public class GPUImageView extends FrameLayout {
    static final String TAG = "GPUImageView";

    public GLSurfaceView mGLSurfaceView;
    public GPUImage mGPUImage;
    public GPUImageFilterGroupBase mFilter;

    OnGestureListener mOuterGestureLsn;
    GestureDetector mGestureDector;
    ScaleGestureDetector mScaleGestureDector;

    public GPUImageView(Context context) {
        super(context);
        init(context, null);
    }

    public GPUImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mGLSurfaceView = new GLSurfaceView(context, attrs);
        addView(mGLSurfaceView);

        mGPUImage = new GPUImage(getContext());
        mGPUImage.setGLSurfaceView(mGLSurfaceView);

        mGestureDector = new GestureDetector(context, mGestureLsn);
        mGestureDector.setOnDoubleTapListener(mDoubleTapLsn);
        mScaleGestureDector = new ScaleGestureDetector(context, mScaleGestureLsn);
    }

    public void runOnGLThread(Runnable runnable) {
        if(null != mGLSurfaceView) {
            mGLSurfaceView.queueEvent(runnable);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDector.onTouchEvent(event) || mScaleGestureDector.onTouchEvent(event)) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (null != mOuterGestureLsn) {
                    mOuterGestureLsn.onActionUp();
                }
            }
            return true;
        }

        if (event.getPointerCount() > 1) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * Retrieve the GPUImage instance used by this view.
     *
     * @return used GPUImage instance
     */
    public GPUImage getGPUImage() {
        return mGPUImage;
    }

    /**
     * Set the scale type of GPUImage.
     *
     * @param scaleType the new ScaleType
     */
    public void setScaleType(GPUImage.ScaleType scaleType) {
        mGPUImage.setScaleType(scaleType);
    }

    /**
     * Set the filter to be applied on the image.
     *
     * @param filter Filter that should be applied on the image.
     */
    public void setFilter(GPUImageFilterGroupBase filter) {
        mFilter = filter;
        mGPUImage.setFilter(filter);
        requestRender();
    }

    /**
     * Get the current applied filter.
     *
     * @return the current filter
     */
    public GPUImageFilter getFilter() {
        return mFilter;
    }

    public void requestRender() {
        mGLSurfaceView.requestRender();
    }

    /**
     * Capture the current image with the size as it is displayed and retrieve it as Bitmap.
     *
     * @return current output as Bitmap
     * @throws InterruptedException
     */
    public Bitmap capture() throws InterruptedException {
        // 如果surface都没创建,那么render线程应该也没创建出来,会导致watier.acquire()一直卡死在那
        if (null == mGPUImage || null == mGPUImage.mRenderer || !mGPUImage.mRenderer.isSurfaceCreated()) {
            Log.i(TAG, "surface not create, can't capture");
            throw new InterruptedException();
        }

        final Semaphore waiter = new Semaphore(0);

        final int width = mGPUImage.getRenderer().getOutputWidth();
        final int height = mGPUImage.getRenderer().getOutputHeight();
        if (width <= 0 || height <= 0) {
            Log.w(TAG, "width or height is zero!");
            throw new InterruptedException();
        }

        // Take picture on OpenGL thread
        final int[] pixelMirroredArray = new int[width * height];
        mGPUImage.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                final IntBuffer pixelBuffer = IntBuffer.allocate(width * height);
                GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
                int[] pixelArray = pixelBuffer.array();

                // Convert upside down mirror-reversed image to right-side up normal image.
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        pixelMirroredArray[(height - i - 1) * width + j] = pixelArray[i * width + j];
                    }
                }
                waiter.release();
            }
        });
        requestRender();
        waiter.acquire();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(pixelMirroredArray));
        return bitmap;
    }

    /**
     * Pauses the GLSurfaceView.
     */
    public void onPause() {
        Log.i(TAG, "pause gpuimage view and destroy filters");

        mGPUImage.getRenderer().destroyFilters();
        mGLSurfaceView.onPause();
    }

    public void uninit() {
        mGPUImage.uninit();
    }

    public void clearImage() {
        if (null != mGPUImage && null != mGPUImage.mRenderer) {
            mGPUImage.mRenderer.deleteImage();
        }
    }

    /**
     * Resumes the GLSurfaceView.
     */
    public void onResume() {
        mGLSurfaceView.onResume();
    }

    GestureDetector.OnDoubleTapListener mDoubleTapLsn = new GestureDetector.OnDoubleTapListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (null != mOuterGestureLsn) {
                mOuterGestureLsn.onDoubleTap();
                return true;
            }
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    };

    ScaleGestureDetector.OnScaleGestureListener mScaleGestureLsn = new ScaleGestureDetector.OnScaleGestureListener() {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (null != mOuterGestureLsn) {
                mOuterGestureLsn.onScale(detector.getScaleFactor());
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    };

    GestureDetector.OnGestureListener mGestureLsn = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            if (null != mOuterGestureLsn) {
                mOuterGestureLsn.showPress();
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (null != mOuterGestureLsn) {
                mOuterGestureLsn.onSingleTap(e);
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (null != mOuterGestureLsn) {
                mOuterGestureLsn.onLongPress();
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    };

    public interface OnGestureListener {
        boolean onSingleTap(MotionEvent e);

        void onDoubleTap();

        void onScale(float factor);

        void showPress();

        void onLongPress();

        void onActionUp();
    }
}
