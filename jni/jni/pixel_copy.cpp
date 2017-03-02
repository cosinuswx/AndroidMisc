#include "jni.h"
#include <stdlib.h>

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
JNIEXPORT void JNICALL Java_com_winomtech_androidmisc_plugin_jni_JniEntry_mixUV(JNIEnv *env,
                                                                                jclass __unused clazz,
                                                                                jobject dstBuf,
                                                                                jint dstPos,
                                                                                jobject uBuf,
                                                                                jobject vBuf,
                                                                                jint pixelStride,
                                                                                jint size) {
    char *pUBuf = (char *) env->GetDirectBufferAddress(uBuf);
    char *pVBuf = (char *) env->GetDirectBufferAddress(vBuf);
    char *pDstBuf = (char *) env->GetDirectBufferAddress(dstBuf) + dstPos;

    for (int i = 0; i < size; ++i) {
        *pDstBuf = *pUBuf;
        pDstBuf++;
        *pDstBuf = *pVBuf;
        pDstBuf++;
        pUBuf += pixelStride;
        pVBuf += pixelStride;
    }
}
