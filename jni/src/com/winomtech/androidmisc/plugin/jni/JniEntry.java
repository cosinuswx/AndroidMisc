package com.winomtech.androidmisc.plugin.jni;

import java.nio.ByteBuffer;

/**
 * @author kevinhuang
 * @since 2015-02-13
 */
public class JniEntry {
    public static native void XXTea(byte[] data, byte[] key, int isDecode);

    public static native void CopyImage(ByteBuffer srcBuf,
                                        int srcPos,
                                        ByteBuffer dstBuf,
                                        int dstPos,
                                        int width,
                                        int height,
                                        int pixelStride,
                                        int widthStride);

    public static native void CopyData(ByteBuffer srcBuf,
                                       int srcPos,
                                       ByteBuffer dstBuf,
                                       int dstPos,
                                       int size);

    public static native void mixUV(ByteBuffer dstBuf,
                                    int dstPos,
                                    ByteBuffer uBuf,
                                    ByteBuffer vBuf,
                                    int width,
                                    int height,
                                    int pixelStride,
                                    int widthStride);
}
