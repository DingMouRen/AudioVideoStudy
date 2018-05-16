#ifndef OPENSLRECORD_RECORDBUFFER_H //防止同一.h头文件被多次包含
#define OPENSLRECORD_RECORDBUFFER_H  //为宏定义命令

class RecordBuffer{
public:
    short **buffer;//只向指针的指针
    int index = -1;
public:
    RecordBuffer(int buffersize);
    ~RecordBuffer();//析构函数，会在每次删除所创建的对象时执行，用于跳出程序前释放资源，比如关闭文件、释放内存等
    /*得到一个新的录制buffer*/
    short* getRecordBuffer();
    /*得到当前录制的buffer*/
    short* getNowBuffer();//short*是指将short类型转成指针
};

#endif //结束一个if...else条件编译块