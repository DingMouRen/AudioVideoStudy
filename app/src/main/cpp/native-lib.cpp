#include <jni.h>
#include <string>
#include "AndroidLog.h"
#include "RecordBuffer.h"
#include "unistd.h"

/**
 * OpenSLES提供的是基于C语言的API,
 * extern "C"：C语言中已经有了头文件以及它的库，我们在C++中直接使用的话，就用extern "C",如果按照C++的符号进行修饰的话，
 * 在库中就会找不到该符号
 */
extern "C"
{
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
}

/*
 * jni的示例
 * JNIEXPORT: JNI关键字，表示此函数是要被JNI调用的
 * jstring: 表示方法的返回类型
 * JNICALL: JNI关键字，表示此函数是要被jni调用的
 * JAVA :为JNI中标识此方法的来源于java的标识头
 * com_...MainActivity: 是方法所在的泪的包名+类名
 * stringFromJNI :是方法名
 */
extern "C"
JNIEXPORT jstring JNICALL
Java_com_dingmouren_audiovideostudy_MainActivity_stringFromJNI(
        JNIEnv *env,
jobject /* this */) {
std::string hello = "Hello from C++  哎呀 妈呀";
return env->NewStringUTF(hello.c_str());
}

/*
 * OpenSL相关
 */
/*引擎对象接口*/
static SLObjectItf  engineObject = NULL;

/*引擎对象*/
static SLEngineItf engineEngine;

/*录音器对象接口*/
static SLObjectItf recorderObject = NULL;

/*录音器对象*/
static SLRecordItf recorderRecord;

/*缓冲队列*/
static SLAndroidSimpleBufferQueueItf  recorderBufferQueue;

/*录制大小为4096
 * #define有很多用法，这里是定义了一个简单函数，使用了括号。用法总结：https://blog.csdn.net/ylwdi/article/details/7027384
 * */
#define RECORDER_FRAMES (2048)
static unsigned recorderSize = RECORDER_FRAMES * 2;

/*PCM文件*/
FILE *pcmFile;

/*录音的buffer*/
RecordBuffer *recordBuffer;

bool finished = false;

/*录音的回调
 * fwrite()函数：
 * size_t fwrite(const void* buffer, size_t size, size_t count, FILE* stream);
 * buffer:指向数据块的指针
 * size:每个数据的大小，单位是byte,例如sizeof(int)是4
 * count:数据大小，数据个数
 * stream:文件指针
 * */
void bqRecorderCallback(SLAndroidSimpleBufferQueueItf bq, void *context){
    LOGD("录制大小：%d",recorderSize);
    //以二进制值写入文件，fwrite调用格式不同返回结果也不同，此处成功写入的话返回实际写入的数据大小，单位是byte,也就是recorderSize
    fwrite(recordBuffer->getNowBuffer(),1,recorderSize,pcmFile);

    if(finished){
        (*recorderRecord)->SetRecordState(recorderRecord,SL_RECORDSTATE_STOPPED);
        //刷新缓冲区后，关闭流
        fclose(pcmFile);
        LOGD("停止录音");
    } else{
        (*recorderBufferQueue)->Enqueue(recorderBufferQueue,recordBuffer->getRecordBuffer(),recorderSize);
    }
}
/*开始录音
 * */
extern "C"
JNIEXPORT void JNICALL
Java_com_dingmouren_audiovideostudy_audio_OpenSLActivity_rdSound(JNIEnv *env, jobject instance,
                                                                 jstring path_) {
    const char *path = env->GetStringUTFChars(path_, 0);

    /*PCM文件*/
    pcmFile = fopen(path,"w");

    /*PCMBuffer队列*/
    recordBuffer = new RecordBuffer(RECORDER_FRAMES * 2);

    SLresult  result;

    /*创建引擎对象*/
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);

    /*s设置IO设备（麦克风）*/
    SLDataLocator_IODevice loc_dev = {SL_DATALOCATOR_IODEVICE, SL_IODEVICE_AUDIOINPUT,
                                      SL_DEFAULTDEVICEID_AUDIOINPUT, NULL};
    SLDataSource audioSrc = {&loc_dev, NULL};

    /*设置buffer队列*/
    SLDataLocator_AndroidSimpleBufferQueue loc_bq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};// 这里因为配置错了出现的错误pAudioSnk: data locator type 0x800007be not allowed

    /*设置录制规格：PCM、2声道、44100Hz、16bit*/
    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 2, SL_SAMPLINGRATE_44_1,
                                   SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
                                   SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT, SL_BYTEORDER_LITTLEENDIAN};
    SLDataSink audioSnk = {&loc_bq, &format_pcm};

    const SLInterfaceID  id[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};

    /*创建录制器*/
    result = (*engineEngine)->CreateAudioRecorder(engineEngine,&recorderObject,&audioSrc,&audioSnk,1,id,req);

    if(SL_RESULT_SUCCESS != result) return;

    result = (*recorderObject)->Realize(recorderObject,SL_BOOLEAN_FALSE);

    if(SL_RESULT_SUCCESS != result) return;

    result = (*recorderObject)->GetInterface(recorderObject,SL_IID_RECORD,&recorderRecord);
    result = (*recorderObject)->GetInterface(recorderObject,SL_IID_ANDROIDSIMPLEBUFFERQUEUE,&recorderBufferQueue);

    finished = false;

    result = (*recorderBufferQueue)->Enqueue(recorderBufferQueue,recordBuffer->getRecordBuffer(),recorderSize);
    result = (*recorderBufferQueue)->RegisterCallback(recorderBufferQueue,bqRecorderCallback,NULL);

    LOGD("开始录音");

    /*开始录音*/
    (*recorderRecord)->SetRecordState(recorderRecord,SL_RECORDSTATE_RECORDING);

    env->ReleaseStringUTFChars(path_, path);
}
/*
 * 停止录音
 * */
extern "C"
JNIEXPORT void JNICALL
Java_com_dingmouren_audiovideostudy_audio_OpenSLActivity_rdStop(JNIEnv *env, jobject instance) {

    if(NULL != recorderRecord) finished = true;

}