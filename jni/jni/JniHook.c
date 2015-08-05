#include "jni.h"
#include "stdlib.h"
#include <jni.h>
#include <pthread.h>
#include <stdlib.h>
#include <stdio.h>
#include <android/log.h>
#include <unistd.h>

#define  LOG_TAG    "JNIMainActivity"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define DELTA 0x9e3779b9
#define MX (((z>>5^y<<2) + (y>>3^z<<4)) ^ ((sum^y) + (key[(p&3)^e] ^ z)))
#define LOGMX1 LOGI("mx_1: %d\n", (z>>5^(y<<2)))
#define LOGMX2 LOGI("mx_2: %d\n", (y>>3^z<<4))
#define LOGMX3 LOGI("mx_3: %d\n", ((sum^y) + (key[(p&3)^e] ^ z)))

void dumpLong(jint* data, int len) {
    int i = 0;
    for (i = 0; i < len; ++i) {
        LOGI("%08x\n", data[i]);
    }
    LOGI("----------------\n");
}

int java_test(int k)
{
    int i;
    int values[] = {1, 5, 9, 12, 89, 101, 232, 1254};
    k = 0;
    for (i = 0; i < 8; ++i)
    {
        k += values[i];
    }
    return k;
}

void btea(jint *v, jint n, jint* key) {
    jint y, z, sum;
    unsigned p, rounds, e;
    jint mx = 0;
    if (n > 1) {          /* Coding Part */
        rounds = 6 + 52 / n;
        sum = 0;
        z = v[n - 1];
        do {
            sum += DELTA;
            e = (sum >> 2) & 3;
            for (p = 0; p < n - 1; p++) {
                y = v[p + 1];
                LOGI("z: %d y: %d\n", z, y);
                LOGMX1;
                LOGMX2;
                LOGMX3;
                mx = MX;
                LOGI("mx1: %08x", mx);
                z = v[p] += mx;
            }
            y = v[0];
            mx = MX;
            LOGI("mx2: %08x", mx);
            z = v[n - 1] += mx;
            dumpLong(v, n);
        } while (--rounds);
    } else if (n < -1) {  /* Decoding Part */
        n = -n;
        rounds = 6 + 52 / n;
        sum = rounds * DELTA;
        y = v[0];
        do {
            e = (sum >> 2) & 3;
            for (p = n - 1; p > 0; p--) {
                z = v[p - 1];
                y = v[p] -= MX;
            }
            z = v[n - 1];
            y = v[0] -= MX;
            sum -= DELTA;
        } while (--rounds);
    }
}

char int2char(jbyte value) {
    if (value < 0) {
        return '?';
    }
    return value <= 9 ? value + '0' : value - 10 + 'a';
}

void dumpHex(jbyte* data, int len) {
    char* temp = malloc(3 * len + 1);
    int i = 0;
    for (i = 0; i < len; ++i) {
        temp[i * 3] = int2char((data[i] & 0xf0) >> 4);
        temp[i * 3 + 1] = int2char(data[i] & 0xf);
        temp[i * 3 + 2] = ' ';
    }
    temp[3 * i] = 0;
    LOGI("%s\n", temp);
    free(temp);
}

JNIEXPORT jint JNICALL Java_com_winomtech_androidmisc_plugin_jni_JniHook_nativeStart(JNIEnv* env,
                                                                                     jclass __unused class)
{
    uint32_t a = 123;
    return a + java_test(40);
}

JNIEXPORT void JNICALL Java_com_winomtech_androidmisc_plugin_jni_JniHook_nativeXXTea(JNIEnv *env,
                                                                                     jclass __unused class,
                                                                                     jbyteArray data, 
                                                                                     jbyteArray key,
                                                                                     jint isDecode) {
    jint len = (*env)->GetArrayLength(env, data);
    jint* data32 = (jint*) (*env)->GetByteArrayElements(env, data, NULL);
    jint* key32 = (jint*) (*env)->GetByteArrayElements(env, key, NULL);
    dumpHex((jbyte*)data32, len);
    dumpHex((jbyte*)key32, 4 * 4);
    dumpLong(data32, len / 4);
    
    btea(data32, isDecode == 1 ? -len / 4 : len / 4, key32);
    dumpHex((jbyte*)data32, len);
    
    (*env)->ReleaseByteArrayElements(env, data, (jbyte*) data32, 0);
    (*env)->ReleaseByteArrayElements(env, key, (jbyte*) key32, JNI_ABORT);
}

