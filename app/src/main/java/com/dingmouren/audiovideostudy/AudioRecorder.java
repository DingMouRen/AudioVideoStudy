package com.dingmouren.audiovideostudy;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 * 用于实现录音 暂停录音
 */

public class AudioRecorder {

    private static final String TAG = "AudioRecorder";

    private static AudioRecorder audioRecorder;
    /*音频输入：麦克风*/
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    /*采样频率，目前标准是44100，某些设备支持22050,16000,11025，一般分为22.05KHz、44.1KHz、48KHz*/
    private final static int AUDIO_SAMPLE_RATE = 16000;
    /*声道  单声道*/
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    /*编码*/
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    /*缓冲区字节大小*/
    private int bufferSizeInBytes = 0;
    /*录制对象*/
    private AudioRecord audioRecord;
    /*录制状态,初始化状态，未预备*/
    private Status status = Status.STATUS_NO_READY;
    /*文件名*/
    private String fileName;
    /*录制文件的集合*/
    private List<String> filesName = new ArrayList<>();

    /*录制对象的状态*/
    public enum Status{
        /*未预备*/
        STATUS_NO_READY,
        /*预备*/
        STATUS_READY,
        /*开始录音*/
        STATUS_START,
        /*暂停录音*/
        STATUS_PAUSE,
        /*停止录音*/
        STATUS_STOP
    }

    private AudioRecorder(){}

    /*单例模式*/
    private static AudioRecorder getInstance(){
        synchronized (AudioRecorder.class){
            if (audioRecorder == null){
                audioRecorder = new AudioRecorder();
                return audioRecorder;
            }
        }
        return audioRecorder;
    }

    /**
     * 创建录音对象
     */
    public void createAudio(String fileName,int audioSource,int sampleRateInHz,int channelConfig,int audioFormat){
        /*获取缓冲区字节大小*/
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,channelConfig,audioFormat);
        audioRecord = new AudioRecord(audioSource,sampleRateInHz,channelConfig,audioFormat,bufferSizeInBytes);
        this.fileName = fileName;
    }

    /**
     * 创建默认的录音对象
     */
    public void createDefaultAudio(String fileName){
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,AUDIO_CHANNEL,AUDIO_ENCODING);
        audioRecord = new AudioRecord(AUDIO_INPUT,AUDIO_SAMPLE_RATE,AUDIO_CHANNEL,AUDIO_ENCODING,bufferSizeInBytes);
        this.fileName = fileName;
        status = Status.STATUS_READY;
    }

    /**
     * 开始录音
     */
    public  void startRecord(){

        if (status == Status.STATUS_NO_READY || TextUtils.isEmpty(fileName)){
            throw new IllegalStateException("录音尚未初始化，请检查是否开启录音权限");
        }

        if (status == Status.STATUS_START){
            throw new IllegalStateException("正在录音");
        }

        Log.e(TAG,"调用函数startRecord--此时状态："+audioRecord.getState());

        audioRecord.startRecording();

        /*开启线程将数据写入文件*/
        new Thread(new Runnable() {
            @Override
            public void run() {
            }
        }).start();
    }

    /**
     * 暂停录音
     */
    public void pauseRecord(){

        Log.e(TAG,"调用函数pauseRecord");
        if (status != Status.STATUS_START){
            throw new IllegalStateException("录音尚未开始");
        }else {
            audioRecord.stop();
            status = Status.STATUS_PAUSE;
        }
    }

    /**
     * 停止录音
     */
    public void stopRecord(){
        Log.e(TAG,"调用函数stopRecord");
        if (status == Status.STATUS_NO_READY || status == Status.STATUS_READY){
            throw new IllegalStateException("录音尚未开始");
        }else {
            audioRecord.stop();
            status = Status.STATUS_STOP;
            release();
        }
    }

    /**
     * 释放资源
     */
    public void release(){

    }


}
