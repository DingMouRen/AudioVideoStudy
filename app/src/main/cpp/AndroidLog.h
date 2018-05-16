//日志头文件，
#ifndef WLPLAYER_ANDROIDLOG_H //用于保证同一头文件不被包含多次。如果两个cpp源文件同时include同一个.h头文件，将两个cpp源文件编译成可执行文件时会出现声明冲突
#define WLPLAYER_ANDROIDLOG_H //为宏定义命令
#include <android/log.h>
#define LOGD(FORMAT,...) __android_log_print(ANDROID_LOG_DEBUG,"dingmouren",FORMAT,##__VA_ARGS__)
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"dingmouren",FORMAT,##__VA_ARGS_)
#define LOG_DEBUG false
#endif //结束一个#if……#else条件编译块