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

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.annotation.IntDef;

import com.winomtech.androidmisc.plugin.camera.SubcoreCamera;
import com.winomtech.androidmisc.plugin.camera.filter.FilterConstants;
import com.winomtech.androidmisc.plugin.camera.filter.GPUImageFilter;
import com.winomtech.androidmisc.plugin.camera.filter.GPUImageFilterGroup;
import com.winomtech.androidmisc.plugin.camera.filter.GPUImageFilterGroupBase;
import com.winomtech.androidmisc.plugin.camera.utils.OpenGlUtils;
import com.winomtech.androidmisc.plugin.camera.utils.Rotation;
import com.winomtech.androidmisc.plugin.camera.utils.RsYuv;
import com.winomtech.androidmisc.plugin.camera.utils.TextureRotationUtil;
import com.winom.olog.Log;
import com.winomtech.androidmisc.common.utils.ObjectCacher;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

@TargetApi(11)
public class GPUImageRenderer implements GLSurfaceView.Renderer, PreviewCallback {
    private static final String TAG = "GPUImageRenderer";

    /**
     * 摄像头数据监听器
     */
    public interface OnPrevFrameListener {
        /**
         * 一帧数据的回复
         *
         * @param data YUV数据，上层不能保存该引用，如果需要保存，则自己复制一份
         */
        void onPrevFrame(byte[] data, int width, int height);
    }

    @IntDef(value = {
            CMD_PROCESS_FRAME,
            CMD_SETUP_SURFACE_TEXTURE,
            CMD_SET_FILTER,
            CMD_DELETE_IMAGE,
            CMD_SET_IMAGE_BITMAP,
            CMD_RERUN_ONDRAW_RUNNABLE,
            CMD_RERUN_DRAWEND_RUNNABLE,
            CMD_RESET_RS_SIZE,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface RenderCmd {
    }

    final static int CMD_PROCESS_FRAME = 0;
    final static int CMD_SETUP_SURFACE_TEXTURE = 1;
    final static int CMD_SET_FILTER = 2;
    final static int CMD_DELETE_IMAGE = 3;
    final static int CMD_SET_IMAGE_BITMAP = 4;
    final static int CMD_RERUN_ONDRAW_RUNNABLE = 5;
    final static int CMD_RERUN_DRAWEND_RUNNABLE = 6;
    final static int CMD_RESET_RS_SIZE = 7;

    /**
     * 命令的一项
     */
    static class CmdItem {
        @RenderCmd
        int cmdId;
        Object param1;
        Object param2;
    }

    static final float CUBE[] = {-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f};

    GPUImageFilterGroupBase mFilter;
    int mGLTextureId;

    SurfaceTexture mSurfaceTexture = null;
    final FloatBuffer mGLCubeBuffer;
    final FloatBuffer mGLTextureBuffer;
    ByteBuffer mGLRgbBuffer;

    int mOutputWidth = 0;
    int mOutputHeight = 0;
    int mImageWidth = 1;
    int mImageHeight = 1;

    final Queue<CmdItem> mRunOnDraw;
    final Queue<CmdItem> mRunOnDrawEnd;

    Rotation mRotation;
    boolean mFlipHorizontal;
    boolean mFlipVertical;
    GPUImage.ScaleType mScaleType = GPUImage.ScaleType.CENTER_CROP;

    OnPrevFrameListener mPrevFrameLsn;

    // 用来缓存当前摄像头的信息，这里假设了一个camera的实例的预览大小一旦设置了，就不会再变
    Camera mCacheCamera = null;
    Point mCachePrevSize;

    RsYuv mRsYuv;
    final FloatBuffer mNormalCubeBuffer;
    final FloatBuffer mNormalTextureFlipBuffer;

    boolean mSurfaceCreated = false;                    // surface是否创建了,如果surface没有创建,意味着render线程还没开始执行

    int mCameraFrameRate = 30;      // 录制的时候,不好改变摄像头的帧率,所以需要在收到数据的时候丢帧
    long mFirstFrameTick = -1;
    long mFrameCount = 0;

    ObjectCacher<CmdItem> mCmdItemCacher = new ObjectCacher<CmdItem>(20) {
        @Override
        public CmdItem newInstance() {
            return new CmdItem();
        }
    };

    public GPUImageRenderer(final GPUImageFilterGroupBase filter) {
        super();

        mRunOnDraw = new LinkedList<CmdItem>();
        mRunOnDrawEnd = new LinkedList<CmdItem>();
        mGLTextureId = OpenGlUtils.NO_TEXTURE;

        mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        setRotation(Rotation.NORMAL, false, false);

        mNormalCubeBuffer = ByteBuffer.allocateDirect(FilterConstants.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mNormalCubeBuffer.put(FilterConstants.CUBE).position(0);

        float[] flipTexture = TextureRotationUtil.getRotation(Rotation.NORMAL, false, true);
        mNormalTextureFlipBuffer = ByteBuffer.allocateDirect(flipTexture.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mNormalTextureFlipBuffer.put(flipTexture).position(0);

        mRsYuv = new RsYuv(SubcoreCamera.getCore().getGlobalRs());

        setFilter(filter);
    }

    public boolean isSurfaceCreated() {
        return mSurfaceCreated;
    }

    @Override
    public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated config: " + config.toString());

        mSurfaceCreated = true;
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        // surface被重建,也就意味着Context发生了变化,则需要将当前的filter全部清理掉
        if (null != mFilter) {
            GPUImageFilterGroup group = new GPUImageFilterGroup();
            group.addFilter(new GPUImageFilter());
            mFilter = group;
            mFilter.init();
        }
    }

    @Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
        Log.d(TAG, "onSurfaceChanged, width: %d, height: %d", width, height);

        mOutputWidth = width;
        mOutputHeight = height;
        GLES20.glViewport(0, 0, width, height);
        adjustImageScaling();

        if (null != mFilter) {
            mFilter.onOutputSizeChanged(width, height);
        }
    }

    @Override
    public void onDrawFrame(final GL10 gl) {
        if (null == mFilter) {
            mFilter = new GPUImageFilterGroup();
            mFilter.addFilter(new GPUImageFilter());
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        runAll(mRunOnDraw);

        if (null != mSurfaceTexture && mFrameCount > 0) {
            mSurfaceTexture.updateTexImage();
        }

        mFilter.draw(mGLTextureId, OpenGlUtils.NO_TEXTURE, mGLCubeBuffer, mGLTextureBuffer);
        runAll(mRunOnDrawEnd);
    }

    public void uninit() {
    }

    public void setOnPrevFrameListener(OnPrevFrameListener listener) {
        mPrevFrameLsn = listener;
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    void runAll(Queue<CmdItem> queue) {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                CmdItem cmdItem = queue.poll();

                switch (cmdItem.cmdId) {
                    case CMD_PROCESS_FRAME:
                        processFrame((byte[]) cmdItem.param1, (Camera) cmdItem.param2);
                        break;
                    case CMD_SETUP_SURFACE_TEXTURE:
                        setUpSurfaceTextureInternal((Camera) cmdItem.param1, (byte[]) cmdItem.param2);
                        break;
                    case CMD_SET_FILTER:
                        setFilterInternal((GPUImageFilterGroupBase) cmdItem.param1);
                        break;
                    case CMD_DELETE_IMAGE:
                        deleteImageInternal();
                        break;
                    case CMD_SET_IMAGE_BITMAP:
                        setImageBitmapInternal((Bitmap) cmdItem.param1, (Boolean) cmdItem.param2);
                        break;
                    case CMD_RERUN_ONDRAW_RUNNABLE:
                        ((Runnable) cmdItem.param1).run();
                        break;
                    case CMD_RERUN_DRAWEND_RUNNABLE:
                        ((Runnable) cmdItem.param1).run();
                        break;
                    case CMD_RESET_RS_SIZE:
                        resetRsSize((Integer) cmdItem.param1, (Integer) cmdItem.param2);
                        break;
                    default:
                        throw new RuntimeException("can't find command");
                }

                mCmdItemCacher.cache(cmdItem);
            }
        }
    }

    void resetRsSize(int width, int height) {
        if (null != mRsYuv) {
            mRsYuv.reset(width, height);
        }
    }

    void deleteImageInternal() {
        GLES20.glDeleteTextures(1, new int[]{mGLTextureId}, 0);
        mGLTextureId = OpenGlUtils.NO_TEXTURE;
    }

    void processFrame(final byte[] data, final Camera camera) {
        if (mImageWidth != mCachePrevSize.x || mImageHeight != mCachePrevSize.y) {
            mImageWidth = mCachePrevSize.x;
            mImageHeight = mCachePrevSize.y;
            adjustImageScaling();
        }

        mRsYuv.execute(data, mGLRgbBuffer.array());

        mGLTextureId = OpenGlUtils.loadTexture(mGLRgbBuffer, mCachePrevSize, mGLTextureId);

        camera.addCallbackBuffer(data);
        mGLRgbBuffer.clear();
    }

    void setUpSurfaceTextureInternal(final Camera camera, byte[] data) {
        if (null == camera) {
            Log.e(TAG, "setUpSurfaceTexture, camera is null");
            return;
        }

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mSurfaceTexture = new SurfaceTexture(textures[0]);
        try {
            camera.addCallbackBuffer(data);
            camera.setPreviewTexture(mSurfaceTexture);
            camera.setPreviewCallbackWithBuffer(GPUImageRenderer.this);
            camera.startPreview();
        } catch (Exception e) {
            Log.e(TAG, "setup camera failed, " + e.getMessage());
        }
    }

    void setFilterInternal(final GPUImageFilterGroupBase filter) {
        final GPUImageFilterGroupBase oldFilter = mFilter;
        mFilter = filter;
        if (oldFilter != null) {
            oldFilter.destroy();
        }
        mFilter.init();
        GLES20.glUseProgram(mFilter.getProgram());
        mFilter.onOutputSizeChanged(mOutputWidth, mOutputHeight);
    }

    void setImageBitmapInternal(final Bitmap bitmap, final boolean recycle) {
        Bitmap resizedBitmap = null;
        if (bitmap.getWidth() % 2 == 1) {
            resizedBitmap = Bitmap.createBitmap(bitmap.getWidth() + 1, bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas can = new Canvas(resizedBitmap);
            can.drawARGB(0x00, 0x00, 0x00, 0x00);
            can.drawBitmap(bitmap, 0, 0, null);
        }

        mGLTextureId = OpenGlUtils.loadTexture(resizedBitmap != null ? resizedBitmap : bitmap,
                mGLTextureId, recycle);
        if (resizedBitmap != null) {
            resizedBitmap.recycle();
        }
        mImageWidth = bitmap.getWidth();
        mImageHeight = bitmap.getHeight();
        adjustImageScaling();
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        // 如果还没到下一帧所要求的时间点,则丢弃这一帧
        if ((System.currentTimeMillis() - mFirstFrameTick) < (mFrameCount + 1) * (1000 / mCameraFrameRate)) {
            camera.addCallbackBuffer(data);
            Log.v(TAG, "too many frame from camera, drop it");
            return;
        }

        if (-1 == mFirstFrameTick) {
            mFirstFrameTick = System.currentTimeMillis();
        }
        mFrameCount++;

        if (mCacheCamera != camera) {
            mCacheCamera = camera;
            Size previewSize = camera.getParameters().getPreviewSize();
            mCachePrevSize = new Point(previewSize.width, previewSize.height);
        }

        if (null != mPrevFrameLsn) {
            mPrevFrameLsn.onPrevFrame(data, mCachePrevSize.x, mCachePrevSize.y);
        }

        if (mGLRgbBuffer == null || mGLRgbBuffer.capacity() != mCachePrevSize.x * mCachePrevSize.y * 4) {
            mGLRgbBuffer = ByteBuffer.allocate(mCachePrevSize.x * mCachePrevSize.y * 4);
        }

        if (mRsYuv.getWidth() != mCachePrevSize.x || mRsYuv.getHeight() != mCachePrevSize.y) {
            runOnDraw(CMD_RESET_RS_SIZE, mCachePrevSize.x, mCachePrevSize.y);
        }

        runOnDraw(CMD_PROCESS_FRAME, data, camera);
        mSurfaceView.requestRender();
    }

    public void setUpSurfaceTexture(final Camera camera, byte[] data) {
        runOnDraw(CMD_SETUP_SURFACE_TEXTURE, camera, data);
    }

    public void setFilter(final GPUImageFilter filter) {
        runOnDraw(CMD_SET_FILTER, filter, null);
    }

    public void deleteImage() {
        runOnDraw(CMD_DELETE_IMAGE, null, null);
    }

    public void setImageBitmap(final Bitmap bitmap) {
        setImageBitmap(bitmap, true);
    }

    public void setImageBitmap(final Bitmap bitmap, final boolean recycle) {
        if (bitmap == null) {
            return;
        }

        runOnDraw(CMD_SET_IMAGE_BITMAP, bitmap, recycle);
    }

    public void addRunnableOnDrawEnd(Runnable runnable) {
        runOnDrawEnd(CMD_RERUN_DRAWEND_RUNNABLE, runnable, null);
    }

    public void setScaleType(GPUImage.ScaleType scaleType) {
        mScaleType = scaleType;
    }

    GLSurfaceView mSurfaceView;

    public void setGlSurfaceView(GLSurfaceView surfaceView) {
        mSurfaceView = surfaceView;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void adjustImageScaling() {
        float outputWidth = mOutputWidth;
        float outputHeight = mOutputHeight;
        if (mRotation == Rotation.ROTATION_270
                || mRotation == Rotation.ROTATION_90) {
            outputWidth = mOutputHeight;
            outputHeight = mOutputWidth;
        }

        float ratio1 = outputWidth / mImageWidth;
        float ratio2 = outputHeight / mImageHeight;
        float ratioMax = Math.max(ratio1, ratio2);
        int imageWidthNew = Math.round(mImageWidth * ratioMax);
        int imageHeightNew = Math.round(mImageHeight * ratioMax);

        float ratioWidth = imageWidthNew / outputWidth;
        float ratioHeight = imageHeightNew / outputHeight;

        float[] cube = CUBE;
        float[] textureCords = TextureRotationUtil.getRotation(mRotation,
                mFlipHorizontal, mFlipVertical);
        if (mScaleType == GPUImage.ScaleType.CENTER_CROP) {
            float distHorizontal = (1 - 1 / ratioWidth) / 2;
            float distVertical = (1 - 1 / ratioHeight) / 2;
            textureCords = new float[]{
                    addDistance(textureCords[0], distHorizontal),
                    addDistance(textureCords[1], distVertical),
                    addDistance(textureCords[2], distHorizontal),
                    addDistance(textureCords[3], distVertical),
                    addDistance(textureCords[4], distHorizontal),
                    addDistance(textureCords[5], distVertical),
                    addDistance(textureCords[6], distHorizontal),
                    addDistance(textureCords[7], distVertical),};
        } else {
            cube = new float[]{CUBE[0] / ratioHeight, CUBE[1] / ratioWidth,
                    CUBE[2] / ratioHeight, CUBE[3] / ratioWidth,
                    CUBE[4] / ratioHeight, CUBE[5] / ratioWidth,
                    CUBE[6] / ratioHeight, CUBE[7] / ratioWidth,};
        }

        mGLCubeBuffer.clear();
        mGLCubeBuffer.put(cube).position(0);
        mGLTextureBuffer.clear();
        mGLTextureBuffer.put(textureCords).position(0);

        float normalImageHeight = mImageHeight;
        float normalImageWidth = mImageWidth;
        if (mRotation == Rotation.ROTATION_270 || mRotation == Rotation.ROTATION_90) {
            normalImageHeight = mImageWidth;
            normalImageWidth = mImageHeight;
        }
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    public void setRotationCamera(final Rotation rotation,
                                  final boolean flipHorizontal, final boolean flipVertical) {
        mGLTextureId = OpenGlUtils.NO_TEXTURE;
        setRotation(rotation, flipVertical, flipHorizontal);
    }

    public void setFrameRate(int frameRate) {
        mCameraFrameRate = frameRate;
        mFirstFrameTick = -1;
        mFrameCount = 0;
    }

    public void setRotation(final Rotation rotation) {
        mRotation = rotation;
        adjustImageScaling();
    }

    public void setRotation(final Rotation rotation, final boolean flipHorizontal, final boolean flipVertical) {
        mFlipHorizontal = flipHorizontal;
        mFlipVertical = flipVertical;
        setRotation(rotation);
    }

    public Rotation getRotation() {
        return mRotation;
    }

    void runOnDraw(@RenderCmd int cmdId, Object param1, Object param2) {
        CmdItem item = mCmdItemCacher.obtain();
        item.cmdId = cmdId;
        item.param1 = param1;
        item.param2 = param2;

        synchronized (mRunOnDraw) {
            mRunOnDraw.add(item);
        }
    }

    void runOnDrawEnd(@RenderCmd int cmdId, Object param1, Object param2) {
        CmdItem item = mCmdItemCacher.obtain();
        item.cmdId = cmdId;
        item.param1 = param1;
        item.param2 = param2;

        synchronized (mRunOnDrawEnd) {
            mRunOnDrawEnd.add(item);
        }
    }

    public int getOutputWidth() {
        return mOutputWidth;
    }

    public int getOutputHeight() {
        return mOutputHeight;
    }

    public int getImageWidth() {
        return mImageWidth;
    }

    public int getImageHeight() {
        return mImageHeight;
    }

    public void destroyFilters() {
        Log.i(TAG, "destroyFilters %b", null != mSurfaceView);
        if (null == mSurfaceView) {
            return;
        }

        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "destroyFilters real %b", null != mFilter);

                if (null != mFilter) {
                    mFilter.destroy();
                    mFilter = null;
                }
            }
        });
    }
}
