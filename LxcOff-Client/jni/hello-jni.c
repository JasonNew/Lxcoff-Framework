#include <string.h>
#include <jni.h>



/* This is a trivial JNI example where we use a native method
 * to return a new VM String. 
 */

//JavaVM *g_vm;		// Can't have globals - SIGSEGV on server
 
JNIEXPORT jstring JNICALL 
Java_de_tlabs_thinkAir_synthBenchmark_JniTest_stringFromJNI( JNIEnv* env,
                                                  jobject thiz )
{
    return (*env)->NewStringUTF(env, "Hello from JNI !");
}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    //g_vm = vm;
   	return JNI_VERSION_1_4;
}