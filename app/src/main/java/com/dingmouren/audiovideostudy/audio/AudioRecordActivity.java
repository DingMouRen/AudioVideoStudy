package com.dingmouren.audiovideostudy.audio;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.dingmouren.audiovideostudy.R;

import java.util.Timer;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class AudioRecordActivity extends AppCompatActivity {
    private static final String TAG = "AudioRecordActivity";
    private TextView mTvTime;
    private TextView mTvFilePath;
    private Button mBtnRecord;
    private Button mBtnPlay;
    private Timer mTimer;//计时器
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);

        initView();
    }

    private void initView() {
        mTvTime = findViewById(R.id.tv_time);
        mTvFilePath = findViewById(R.id.tv_file_path);
        mBtnRecord = findViewById(R.id.btn_record);
        mBtnPlay = findViewById(R.id.btn_play);
    }
}
