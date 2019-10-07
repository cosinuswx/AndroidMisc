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

import android.graphics.PointF;
import android.opengl.GLES20;

import com.winom.olog.OLog;
import com.winomtech.androidmisc.plugin.camera.utils.OpenGlUtils;

import java.nio.FloatBuffer;
import java.util.LinkedList;

public class GPUImageFilter {
    private final static String TAG = "GPUImageFilter";

    public static final String NO_FILTER_VERTEX_SHADER = ""
            + "attribute vec4 position;\n"
            + "attribute vec4 inputTextureCoordinate;\n"
            + " \n"
            + "varying vec2 textureCoordinate;\n"
            + " \n"
            + "void main()\n"
            + "{\n"
            + "    gl_Position = position;\n"
            + "    textureCoordinate = inputTextureCoordinate.xy;\n"
            + "}";

    public static final String NO_FILTER_FRAGMENT_SHADER = ""
            + "varying highp vec2 textureCoordinate;\n"
            + " \n"
            + "uniform sampler2D inputImageTexture;\n"
            + " \n"
            + "void main()\n"
            + "{\n"
            + "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n"
            + "}";

    private final LinkedList<Runnable> mRunOnDraw;
    private final String mVertexShader;
    private final String mFragmentShader;

    private int mGLProgId;
    protected int mGLAttribPosition;
    protected int mGLUniformTexture;
    protected int mGLAttribTextureCoordinate;
    private int mIsAndroidLocation;

    private int mOutputWidth;
    private int mOutputHeight;

    private boolean mIsInitialized;
    private long mContextHandle;

    public GPUImageFilter() {
        this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
    }

    public GPUImageFilter(final String vertexShader, final String fragmentShader) {
        mRunOnDraw = new LinkedList<>();
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
    }

    public final void init() {
        mContextHandle = OpenGlUtils.getContextHandle();
        onInit();
        mIsInitialized = true;
        onInitialized();
    }

    protected int loadProgram() {
        return OpenGlUtils.loadProgram(mVertexShader, mFragmentShader);
    }

    public void onInit() {
        mGLProgId = loadProgram();
        mGLAttribPosition = GLES20.glGetAttribLocation(mGLProgId, "position");
        mGLUniformTexture = GLES20.glGetUniformLocation(mGLProgId, "inputImageTexture");
        mGLAttribTextureCoordinate = GLES20.glGetAttribLocation(mGLProgId, "inputTextureCoordinate");
        mIsAndroidLocation = GLES20.glGetUniformLocation(mGLProgId, "isAndroid");
        mIsInitialized = true;
    }

    public void onInitialized() {
    }

    public final void destroy() {
        runPendingOnDrawTasks();

        mIsInitialized = false;
        GLES20.glDeleteProgram(mGLProgId);
        onDestroy();

        if (OpenGlUtils.getContextHandle() != mContextHandle) {
            OLog.e(TAG, "destroy filter on diff context " + this);
        }
    }

    public void onDestroy() {
    }

    public void onOutputSizeChanged(final int width, final int height) {
        mOutputWidth = width;
        mOutputHeight = height;
    }

    public int getTarget() {
        return GLES20.GL_TEXTURE_2D;
    }

    public void onDraw(final int textureId, final FloatBuffer cubeBuffer, final FloatBuffer textureBuffer) {
        onPreDraw();
        GLES20.glUseProgram(mGLProgId);
        runPendingOnDrawTasks();
        if (!mIsInitialized) {
            return;
        }

        cubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
                textureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            OpenGlUtils.bindTexture(getTarget(), textureId);
            GLES20.glUniform1i(mGLUniformTexture, 0);
        }
        onDrawArraysPre(textureId);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);

        OpenGlUtils.bindTexture(getTarget(), 0);
    }

    public void onPreDraw() {
    }

    protected void onDrawArraysPre(int textureId) {
        if (-1 != mIsAndroidLocation) {
            setInteger(mIsAndroidLocation, 1);
        }
    }

    protected void runPendingOnDrawTasks() {
        // 将当前要运行的拷贝到新的数组,然后再开始执行,防止执行的里面再次添加
        LinkedList<Runnable> runList;
        synchronized (mRunOnDraw) {
            runList = new LinkedList<>(mRunOnDraw);
            mRunOnDraw.clear();
        }

        while (!runList.isEmpty()) {
            runList.removeFirst().run();
        }
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public int getProgram() {
        return mGLProgId;
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.addLast(runnable);
        }
    }

    protected void setInteger(int location, int intValue) {
        GLES20.glUniform1i(location, intValue);
    }

    protected static void setFloat(int location, float floatValue) {
        GLES20.glUniform1f(location, floatValue);
    }

    protected static void setFloatVec2(int location, float[] arrayValue) {
        GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue));
    }

    protected static void setPoint(int location, PointF point) {
        float[] vec2 = new float[2];
        vec2[0] = point.x;
        vec2[1] = point.y;
        GLES20.glUniform2fv(location, 1, vec2, 0);
    }

    protected static void setUniformMatrix4f(int location, float[] matrix) {
        GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0);
    }
}
