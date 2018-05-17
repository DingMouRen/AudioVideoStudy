### 音频采集AudioRecord工作流程(PCM)
1.配置AudioRecord参数、初始化内部的音频缓冲区
AudioRecord的构造函数如下：
```
public AudioRecord(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes)
```
参数说明：
* audioSource:指的是音频采集的输入源。可选值以常量的方式定义在MediaRecorder.AudioSource中。常用的有：DEFAULT(默认)、VOICE_RECOGNITION(用于语音识别，等同于DEFAULT)、MIC(从手机麦克风输入)、VOICE_COMMUNICATION(用于VoIP应用)
* samleRateInHz:用于指定以多大的采样频率来采集音频。44.1Hz可以应用于所有手机，有的频率手机不支持。
* channelConfig:用于指定录音器采集几个声道的声音。可选值以常量的形式定义在AudioFormat中。常用的值：CHANNEL_IN_MONO(单声道)、CHANNEL_IN_STEREO(立体声，也就是双声道)。出于性能考虑，一般按照单声道采集，然后在后期的处理中将数据转换成立体声。
* audioFormat:指采样的表示格式。可选值以常量值得形式定义在AudioFormat，常用值：ENCODING_PCM_16BIT(16位)、ENCODING_PCM_8BIT(8位)。16位可以兼容绝大部分手机。
* bufferSizeInBytes:是AudioRecord内部的音频缓冲区的大小。获取音频缓冲区大小
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

1.配置AudioTrack参数、初始化内部的音频缓冲区
AudioTrack构造函数如下：
```
public AudioTrack(int streamType,int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes,int mode) 
```
绝大部分参数与AudioRecord相同，不同的是streamType mode,下面讲一下：
* streamType:是指Android手机上的音频管理策略
```
STREAM_VOCIE_CALL:电话声音
STREAM_SYSTEM:系统声音
STREAM_RING:铃声
STREAM_MUSIC:音乐声
STREAM_ALARM:警告声
STREAM_NOTIFICATION:通知声
```
* mode:是AudioTrack提供的两种播放模式。以常量的形式定义在AudioTrack中，MODE_STATIC和MODE_STREAM。MODE_STATIC模式是一次性将所有的数据都写入缓冲区中，适合播放铃声、系统提示音等比较短的音频。MODE_STREAM需要按照一定时间间隔不间断的写入音频数据，适合任何音频的播放场景。

2.将AudioTrack切换到播放状态，并开启播放线程写入pcm数据,关键函数如下：
```
//开始播放
mAudioTrack.play();

//写入数据,还有其他的函数
mAudioTrack.write(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes)
```
3.先停止播放，再关闭播放线程并销毁资源

##### AudioRecord录制PCM音频参考代码：
```
/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class AudioRecordManager {
    private static final String TAG = "AudioRecordManager";
    /*录音管理类实例*/
    public static AudioRecordManager sInstance;
    /*录音实例*/
    protected AudioRecord mAudioRecord;
    /*录音线程*/
    private Thread mRecordThread;
    /*音频采集的输入源，麦克风*/
    private static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    /*音频采集的采样频率*/
    public static int SAMPLE_RATE_IN_HZ = 44100;
    /*音频采集的声道数,此处为单声道，后期处理可以转换成双声道的立体声,如果这里是MONO声道的话，会有变声的情况*/
    private static int CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_IN_STEREO;
    /*音频采集的格式，数据位宽16位*/
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    /*音频缓冲区大小*/
    private int mBufferSizeInBytes = 0;
    /*是否正在录音*/
    private boolean mIsRecording = false;
    /*文件输出流*/
    private FileOutputStream mFileOutputStream;
    /*文件输出路径*/
    private String mOutputFilePath;

    /*单例模式*/
    private AudioRecordManager(){}
    public static AudioRecordManager getInstance(){
        if (null == sInstance){
            synchronized (AudioRecordManager.class){
                if (null == sInstance){
                    sInstance = new AudioRecordManager();
                    return sInstance;
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化配置
     */
    public void initConfig() throws AudioConfigurationException {

        if (null != mAudioRecord) mAudioRecord.release();

        //使用44.1kHz的采样率初始化录音器
        try {
            mBufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,CHANNEL_CONFIGURATION,AUDIO_FORMAT);
            mAudioRecord = new AudioRecord(AUDIO_SOURCE,SAMPLE_RATE_IN_HZ,CHANNEL_CONFIGURATION,AUDIO_FORMAT,mBufferSizeInBytes);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.e(TAG,"采样率44.1kHZ的AudioRecord初始化失败");
        }

/*        //44.1kHz采样率没有成功的话，再使用16kHz的采样率初始化录音器
        if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED){
            try {
                SAMPLE_RATE_IN_HZ = 16000;
                mBufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,CHANNEL_CONFIGURATION,AUDIO_FORMAT);
                mAudioRecord = new AudioRecord(AUDIO_SOURCE,SAMPLE_RATE_IN_HZ,CHANNEL_CONFIGURATION,AUDIO_FORMAT,mBufferSizeInBytes);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Log.e(TAG,"采样率16kHZ的AudioRecord初始化失败");
            }
        }*/

        //都失败的话，抛出异常
        if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) throw new AudioConfigurationException();

    }

    /**
     * 开始录音
     */
    public void startRecord(String filePath) throws AudioStartRecordingException {

        if (mAudioRecord != null && mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED){
            try {
                mAudioRecord.startRecording();
            } catch (Exception e) {
                throw new AudioStartRecordingException();
            }
        }else {
            throw new AudioStartRecordingException();
        }

        mIsRecording = true;
        mRecordThread = new Thread(new RecordRunnable(),"RecordThread");

        try {
            this.mOutputFilePath = filePath;
            mRecordThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new AudioStartRecordingException();
        }
    }

    /**
     * 结束录音
     */
    public void stopRecord(){
        try {
            if (mAudioRecord != null){

                mIsRecording = false;

                //关闭线程
                try {
                    if (mRecordThread != null){
                        mRecordThread.join();
                        mRecordThread = null;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //关闭录音，释放资源
                releaseAudioRecord();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭录音，释放资源
     */
    private void releaseAudioRecord(){
        if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }


    class RecordRunnable implements Runnable{
        @Override
        public void run() {
            try {
                mFileOutputStream = new FileOutputStream(mOutputFilePath);
                byte[] audioDataArray = new byte[mBufferSizeInBytes];
                while (mIsRecording){
                    int audioDataSize = getAudioRecordBufferSize(mBufferSizeInBytes,audioDataArray);
                    if (audioDataSize > 0) {
                        mFileOutputStream.write(audioDataArray);
                    }else {
                        mIsRecording = false;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if (null != mFileOutputStream){
                        mFileOutputStream.close();
                        mFileOutputStream = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取缓存区的大小
     * @param bufferSizeInBytes
     * @param audioDataArray
     * @return
     */
    private int getAudioRecordBufferSize(int bufferSizeInBytes, byte[] audioDataArray) {
        if (mAudioRecord != null){
            int size = mAudioRecord.read(audioDataArray,0,bufferSizeInBytes);
            return size;
        }else {
            return 0;
        }
    }

    /**
     * 是否正在录音
     */
    public boolean isRecording(){
        return mIsRecording;
    }
}

```

##### AudioTrack播放PCM音频参考代码：

```
/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class AudioTrackManager {
    private static final String TAG = "AudioTrackManager";
    public static AudioTrackManager sInstance;
    private AudioTrack mAudioTrack;
    /*音频管理策略*/
    private static int STREAM_TYPE = AudioManager.STREAM_MUSIC;
    /*音频的采样率，44.1kHz可以所有手机*/
    private static int SAMPLE_RATE_IN_HZ = 44100;
    /*音频的声道数，此处为单声道*/
    private static int CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_IN_STEREO;
    /*采样格式，数据位宽是16位*/
    private static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    /*音频缓存区大小*/
    private int mBufferSizeInBytes = 0;
    /*是否正在播放*/
    private boolean mIsPlaying = false;
    private Thread mPlayingThread;
    /*播放文件的路径*/
    private String mFilePath;
    /*读取文件IO流*/
   private DataInputStream mDataInputStream;

    /*单例模式*/
    private AudioTrackManager(){}
    public static AudioTrackManager getInstance(){
        if (null == sInstance){
            synchronized (AudioTrackManager.class){
                if (null == sInstance){
                    sInstance = new AudioTrackManager();
                    return sInstance;
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化配置
     */
    public void initConfig() throws AudioConfigurationException {

        if (null != mAudioTrack) mAudioTrack.release();

        mBufferSizeInBytes = AudioTrack.getMinBufferSize(SAMPLE_RATE_IN_HZ,CHANNEL_CONFIGURATION,AUDIO_FORMAT);
        mAudioTrack = new AudioTrack(STREAM_TYPE,SAMPLE_RATE_IN_HZ,CHANNEL_CONFIGURATION,AUDIO_FORMAT,mBufferSizeInBytes,AudioTrack.MODE_STREAM);

        if (mAudioTrack == null || mAudioTrack.getState() != AudioTrack.STATE_INITIALIZED) throw new AudioConfigurationException();

    }

    /**
     * 开始播放录音
     */
    public synchronized void play(String filePath){
        Log.e(TAG,"播放状态："+mAudioTrack.getState());
        if (mIsPlaying) return;
        if (null != mAudioTrack && mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED){
            mAudioTrack.play();
        }
        this.mFilePath = filePath;
        mIsPlaying = true;
        mPlayingThread = new Thread(new PlayingRunnable(),"PlayingThread");
        mPlayingThread.start();
    }

    /**
     * 停止播放
     */
    private void stop(){
        try {
            if (mAudioTrack != null){

                mIsPlaying = false;

                //首先停止播放
                mAudioTrack.stop();

                //关闭线程
                try {
                    if (mPlayingThread != null){
                        mPlayingThread.join();
                        mPlayingThread = null;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //释放资源
                releaseAudioTrack();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放资源
     */
    private void releaseAudioTrack(){
        if (mAudioTrack.getState() == AudioRecord.STATE_INITIALIZED) {
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }


    class PlayingRunnable implements Runnable{
        @Override
        public void run() {
            try {
                FileInputStream fileInputStream = new FileInputStream(new File(mFilePath));
                mDataInputStream = new DataInputStream(fileInputStream);
                byte[] audioDataArray = new byte[mBufferSizeInBytes];
                int readLength = 0;
                while (mDataInputStream.available() > 0){
                    readLength = mDataInputStream.read(audioDataArray);
                    if (readLength > 0) {
                        mAudioTrack.write(audioDataArray,0,readLength);
                    }
                }
                stop();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

```

[Github源码](https://github.com/DingMouRen/AudioVideoStudy)
