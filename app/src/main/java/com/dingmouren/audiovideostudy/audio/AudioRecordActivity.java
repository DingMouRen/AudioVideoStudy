package com.dingmouren.audiovideostudy.audio;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dingmouren.audiovideostudy.R;
import com.dingmouren.audiovideostudy.audio.androidapi.AudioRecordManager;
import com.dingmouren.audiovideostudy.audio.androidapi.AudioTrackManager;
import com.dingmouren.audiovideostudy.audio.exception.AudioConfigurationException;
import com.dingmouren.audiovideostudy.audio.exception.AudioStartRecordingException;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class AudioRecordActivity extends AppCompatActivity {
    private static final String TAG = "AudioRecordActivity";
    private static final int DISPLAY_RECORDING_TIME = 1;//显示录音时长的标记
    private TextView mTvTime;
    private TextView mTvFilePath;
    private Button mBtnRecord;
    private Button mBtnPlay;
    private Timer mTimer;//计时器
    private AudioRecordManager mAudioRecordManager;
    private AudioTrackManager mAudioTrackManager;
    private String mFilePath = Environment.getExternalStorageDirectory()+"/audio.pcm";
    private int mTimerTime = 0;//计时的时间，单位是秒
    private TimerTask mTimerTask;
    private boolean mIsRecording = false;//是否正在录音

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);

        mTimer = new Timer();
        try {
            mAudioRecordManager = AudioRecordManager.getInstance();
            mAudioRecordManager.initConfig();
            mAudioTrackManager = AudioTrackManager.getInstance();
            mAudioTrackManager.initConfig();
        } catch (AudioConfigurationException e) {
            e.printStackTrace();
            Log.e(TAG,e.getMessage());
        }

        initView();

        initListener();
    }

    private void initView() {
        mTvTime = findViewById(R.id.tv_time);
        mTvFilePath = findViewById(R.id.tv_file_path);
        mBtnRecord = findViewById(R.id.btn_record);
        mBtnPlay = findViewById(R.id.btn_play);

        mTvFilePath.setText("文件存储路径："+mFilePath);
    }

    private void initListener(){
        mBtnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    recordAndStop();
                } catch (AudioStartRecordingException e) {
                    e.printStackTrace();
                }
            }
        });
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRecordFile();
            }
        });
    }

    /**
     * 开始和结束录音
     */
    private void recordAndStop() throws AudioStartRecordingException {
        Log.e(TAG,"在录音吗:"+mAudioRecordManager.isRecording());
        if (mIsRecording){//正在录音--》停止录音
            mBtnRecord.setText("开始录音");
            mAudioRecordManager.stopRecord();
            mTimerTask.cancel();
            mTimer.cancel();
            mIsRecording = false;
        }else {//空闲--》开始录音
            mBtnRecord.setText("停止录音");
            mAudioRecordManager.startRecord(mFilePath);
            mTimerTime = 0;//初始化计时时间
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    sHandler.sendEmptyMessage(DISPLAY_RECORDING_TIME);
                    mTimerTime++;
                }
            };
            mTimer.schedule(mTimerTask,0,1000);
            mIsRecording = true;
        }
    }


    /**
     * 播放录音文件
     */
    private void playRecordFile(){
        mAudioTrackManager.play(mFilePath);
    }

    private  Handler sHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == DISPLAY_RECORDING_TIME){
                int minutes = mTimerTime / 60;
                int seconds = mTimerTime % 60;
                String timeRecoding = String.format("%02d:%02d",minutes,seconds);
                mTvTime.setText(timeRecoding);
            }
        }
    };
}
