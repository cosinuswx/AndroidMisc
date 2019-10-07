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

package com.winomtech.androidmisc.plugin.camera.filter;

import android.opengl.GLES20;

import com.winom.olog.OLog;
import com.winomtech.androidmisc.plugin.camera.utils.OpenGlUtils;
import com.winomtech.androidmisc.plugin.camera.utils.Rotation;
import com.winomtech.androidmisc.plugin.camera.utils.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

/**
 * Resembles a filter that consists of multiple filters applied after each
 * other.
 */
public abstract class GPUImageFilterGroupBase extends GPUImageFilter {
    private static final String TAG = "GPUImageFilterGroupBase";

    // 利用双FrameBuffer循环使用应该就可以了,不需要这么多FrameBuffer
    private int[] mDoubleFBBuffer;
    private int[] mDoubleFBTexture;

    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;
    private final FloatBuffer mGLTextureFlipBuffer;

    /**
     * Instantiates a new GPUImageFilterGroup with no filters.
     */
    public GPUImageFilterGroupBase() {
        mGLCubeBuffer = ByteBuffer.allocateDirect(FilterConstants.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(FilterConstants.CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer.put(TextureRotationUtil.TEXTURE_NO_ROTATION).position(0);

        float[] flipTexture = TextureRotationUtil.getRotation(Rotation.NORMAL, false, true);
        mGLTextureFlipBuffer = ByteBuffer.allocateDirect(flipTexture.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureFlipBuffer.put(flipTexture).position(0);
    }

    /**
     * 返回当前使用的filter列表
     */
    public abstract List<GPUImageFilter> getRenderFilters();

    public abstract void addFilter(GPUImageFilter aFilter);

    @Override
    public void onDestroy() {
        destroyFramebuffers();
        super.onDestroy();
    }

    private void destroyFramebuffers() {
        if (mDoubleFBTexture != null) {
            for (int i = 0; i < mDoubleFBBuffer.length; ++i) {
                OLog.d(TAG, "delete textureId: %d, fbId: %d", mDoubleFBTexture[i], mDoubleFBBuffer[i]);
            }

            GLES20.glDeleteTextures(mDoubleFBTexture.length, mDoubleFBTexture, 0);
            mDoubleFBTexture = null;
            GLES20.glDeleteFramebuffers(mDoubleFBBuffer.length, mDoubleFBBuffer, 0);
            mDoubleFBBuffer = null;
        }
    }

    @Override
    public void onOutputSizeChanged(final int width, final int height) {
        super.onOutputSizeChanged(width, height);
        if (mDoubleFBBuffer != null) {
            destroyFramebuffers();
        }

        List<GPUImageFilter> renderFilters = getRenderFilters();
        int size = renderFilters.size();
        for (int i = 0; i < size; i++) {
            renderFilters.get(i).onOutputSizeChanged(width, height);
        }

        if (size > 0) {
            mDoubleFBBuffer = new int[2];
            mDoubleFBTexture = new int[2];

            for (int i = 0; i < mDoubleFBBuffer.length; i++) {
                GLES20.glGenFramebuffers(1, mDoubleFBBuffer, i);
                GLES20.glGenTextures(1, mDoubleFBTexture, i);
                OpenGlUtils.bindTextureToFrameBuffer(mDoubleFBBuffer[i], mDoubleFBTexture[i], width, height);
                OLog.d(TAG, "new textureId: %d, fbId: %d", mDoubleFBTexture[i], mDoubleFBBuffer[i]);
            }
        }
    }

    @Override
    public void onDraw(final int textureId, final FloatBuffer cubeBuffer, final FloatBuffer textureBuffer) {
        throw new RuntimeException("this method should not been call!");
    }

    /**
     * 绘制当前特效
     * @param textureId 图像输入
     * @param outFrameBufferId 需要绘制到哪里,如果为-1,表示需要绘制到屏幕
     * @param cubeBuffer 绘制的矩阵
     * @param textureBuffer 需要使用图像输入的哪一部分
     */
    public void draw(final int textureId,
                     final int outFrameBufferId,
                     final FloatBuffer cubeBuffer,
                     final FloatBuffer textureBuffer) {
        onPreDraw();
        runPendingOnDrawTasks();
        if (!isInitialized() || mDoubleFBBuffer == null || mDoubleFBTexture == null || null == getRenderFilters()) {
            return;
        }

        if (textureId == OpenGlUtils.NO_TEXTURE) {
            return;
        }

        List<GPUImageFilter> filters = getRenderFilters();
        int size = filters.size();
        int previousTexture = textureId;
        for (int i = 0; i < size; i++) {
            GPUImageFilter filter = filters.get(i);
            boolean isNotLast = i < size - 1;
            if (isNotLast) {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mDoubleFBBuffer[i % 2]);
                GLES20.glClearColor(0, 0, 0, 0);
            } else if (OpenGlUtils.NO_TEXTURE != outFrameBufferId) {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outFrameBufferId);
                GLES20.glClearColor(0, 0, 0, 0);
            }

            if (i == 0) {
                filter.onDraw(previousTexture, cubeBuffer, textureBuffer);
            } else if (i == size - 1) {
                filter.onDraw(previousTexture, mGLCubeBuffer, (size % 2 == 0) ? mGLTextureFlipBuffer : mGLTextureBuffer);
            } else {
                filter.onDraw(previousTexture, mGLCubeBuffer, mGLTextureBuffer);
            }

            if (isNotLast) {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                previousTexture = mDoubleFBTexture[i % 2];
            } else {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            }
        }
    }
}
