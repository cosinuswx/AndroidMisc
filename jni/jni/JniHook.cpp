#include "Common.h"
#include "stdlib.h"

extern "C"
int java_test(int k)
{
    int values[] = {1, 5, 9, 12, 89, 101, 232, 1254};
    k = 0;
    for (int i = 0; i < 8; ++i)
    {
        k += values[i];
    }
    return k;
}

extern "C"
void Java_com_winomtech_androidmisc_jni_JniHook_nativeStart(JNIEnv* jniEnv)
{
    printf("%d", java_test(40));
}

