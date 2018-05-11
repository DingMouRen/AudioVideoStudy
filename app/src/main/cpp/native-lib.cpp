#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_dingmouren_audiovideostudy_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++  哎呀 妈呀";
    return env->NewStringUTF(hello.c_str());
}
