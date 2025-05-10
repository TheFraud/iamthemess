#include <jni.h>

JNIEXPORT void JNICALL
Java_com_example_testndk_MainActivity_helloNDK(JNIEnv *env, jobject obj) {
    printf("Hello from NDK!\n");
}
