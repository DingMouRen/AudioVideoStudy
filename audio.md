### 音频采集AudioRecord工作流程(PCM)
1.配置参数、初始化内部的音频缓冲区
AudioRecord的构造函数如下：
```
public AudioRecord(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes)
```
参数说明：
* audioSource:指的是音频采集的输入源。可选值以常量的方式定义在MediaRecorder.AudioSource中。常用的有：DEFAULT(默认)、VOICE_RECOGNITION(用于语音识别，等同于DEFAULT)、MIC(从手机麦克风输入)、VOICE_COMMUNICATION(用于VoIP应用)
* samleRateInHz:用于指定以多大的采样频率来采集音频。44.1Hz可以应用于所有手机，有的频率手机不支持。
* channelConfig:用于指定录音器采集几个声道的声音。可选值以常量的形式定义在AudioFormat中。常用的值：CHANNEL_IN_MONO(单声道)、CHANNEL_IN_STEREO(立体声，也就是双声道)。出于性能考虑，一般按照单声道采集，然后在后期的处理中将数据转换成立体声。
* audioFormat:指采样的表示格式。可选值以常量值得形式定义在AudioFormat，常用值：ENCODING_PCM_16BIT(16位)、ENCODING_PCM_8BIT(8位)。16位可以兼容绝大部分手机。
* bufferSizeInBytes：是AudioRecord内部的音频缓冲区的大小。获取音频缓冲区大小
```
public int getMinBufferSize(int sampleRateInHz,int channelConfig,int audioFormat);
```
配置好后，检查一下AudioRecord的当前状态。

2.开始采集
```
mAudioRecord.startRecording();
```

3.提取数据
此时需要一个线程，从AudioRecord的缓冲区中将音频数据不断的读取出来。这里一定要及时，否则会出现“overrun”的错误，意味着应用层没有及时地取走音频数据，导致内部的音频缓冲区溢出。读取数据的函数如下：
```
//byte类型数据
public int read(byte[] audioData,int offsetInBytes,int sizeInBytes);

//short类型数据
public int read(short[] audioData,int offsetInShorts,int sizeInShorts);
```
拿到数据后通过Java层将数据直接写入文件

4.停止采集，释放资源
先读取数据再结束线程，再通过stop停止录音，通过release释放录音器，然后关闭io流。

### 音频采集AudioTrack工作流程(PCM)
