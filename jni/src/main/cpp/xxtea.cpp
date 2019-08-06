#include "xxtea.h"

#define DELTA 0x9e3779b9
#define MX (((z>>5^y<<2) + (y>>3^z<<4)) ^ ((sum^y) + (key[(p&3)^e] ^ z)))

void btea(jint *v, jint n, jint *key);

extern "C"
JNIEXPORT void JNICALL Java_com_winomtech_androidmisc_plugin_jni_JniEntry_XXTea(JNIEnv *env,
                                                                                jclass __unused clazz,
                                                                                jbyteArray data,
                                                                                jbyteArray key,
                                                                                jint isDecode) {
    jint len = env->GetArrayLength(data);
    jint *data32 = (jint *) env->GetByteArrayElements(data, 0);
    jint *key32 = (jint *) env->GetByteArrayElements(key, 0);

    btea(data32, isDecode == 1 ? -len / 4 : len / 4, key32);

    env->ReleaseByteArrayElements(data, (jbyte *) data32, 0);
    env->ReleaseByteArrayElements(key, (jbyte *) key32, JNI_ABORT);
}

void btea(jint *v, jint n, jint *key) {
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
                mx = MX;
                z = v[p] += mx;
            }
            y = v[0];
            mx = MX;
            z = v[n - 1] += mx;
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
