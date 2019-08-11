#include "jni.h"
#include <cstdlib>
#include <cstring>

extern "C"
JNIEXPORT void JNICALL Java_com_winomtech_androidmisc_plugin_jni_JniEntry_CopyData(JNIEnv *env,
                                                                                   jclass __unused clazz,
                                                                                   jobject srcBuf,
                                                                                   jint srcPos,
                                                                                   jobject dstBuf,
                                                                                   jint dstPos,
                                                                                   jint size) {
    char *pSrcBuf = (char *) env->GetDirectBufferAddress(srcBuf);
    char *pDstBuf = (char *) env->GetDirectBufferAddress(dstBuf);

    memcpy(pDstBuf + dstPos, pSrcBuf + srcPos, (size_t)size);
}

extern "C"
JNIEXPORT void JNICALL Java_com_winomtech_androidmisc_plugin_jni_JniEntry_CopyImage(JNIEnv *env,
                                                                                    jclass __unused clazz,
                                                                                    jobject srcBuf,
                                                                                    jint srcPos,
                                                                                    jobject dstBuf,
                                                                                    jint dstPos,
                                                                                    jint width,
                                                                                    jint height,
                                                                                    jint pixelStride,
                                                                                    jint rowStride) {
    char *pSrcBuf = (char *) env->GetDirectBufferAddress(srcBuf) + srcPos;
    char *pDstBuf = (char *) env->GetDirectBufferAddress(dstBuf) + dstPos;
    size_t widthInByte = (size_t) (width * pixelStride);
    size_t strideInByte = (size_t) (rowStride * pixelStride);

    for (int i = 0; i < height; ++i) {
        memcpy(pDstBuf, pSrcBuf, widthInByte);
        pDstBuf += widthInByte;
        pSrcBuf += strideInByte;
    }
}

extern "C"
JNIEXPORT void JNICALL Java_com_winomtech_androidmisc_plugin_jni_JniEntry_mixUV(JNIEnv *env,
                                                                                jclass __unused clazz,
                                                                                jobject dstBuf,
                                                                                jint dstPos,
                                                                                jobject uBuf,
                                                                                jobject vBuf,
                                                                                jint width,
                                                                                jint height,
                                                                                jint pixelStride,
                                                                                jint widthStride) {
    char *pUStartBuf = (char *) env->GetDirectBufferAddress(uBuf);
    char *pVStartBuf = (char *) env->GetDirectBufferAddress(vBuf);
    char *pDstBuf = (char *) env->GetDirectBufferAddress(dstBuf) + dstPos;

    size_t margin = (size_t) (widthStride / 2 * pixelStride);

    for (int i = 0; i < height; i += 2) {
        char* pUBuf = pUStartBuf;
        char* pVBuf = pVStartBuf;

        for (int j = 0; j < width; j += 2) {
            *pDstBuf = *pUBuf;
            pDstBuf++;
            *pDstBuf = *pVBuf;
            pDstBuf++;
            pUBuf += pixelStride;
            pVBuf += pixelStride;
        }

        pUStartBuf += margin;
        pVStartBuf += margin;
    }
}
