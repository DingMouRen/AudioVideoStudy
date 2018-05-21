package com.dingmouren.audiovideostudy;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dingmouren.audiovideostudy.audio.AudioRecordActivity;
import com.dingmouren.audiovideostudy.audio.OpenSLActivity;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    // 要在哪个类运用 JNI ，就得加载相应的动态库（本地）
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Example of a call to a native method
        final TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }


    /**
     * AudioRecord 与AudioTrack的录制和播放pcm音频
     * @param view
     */
    public void audioData(View view){
        startActivity(new Intent(MainActivity.this, AudioRecordActivity.class));
    }


    /**
     * OpenSL的录制和播放pcm音频
     * @param view
     */
    public void audioDataSL(View view){
        startActivity(new Intent(MainActivity.this, OpenSLActivity.class));
    }



   // ------------------------------ JNI的本地方法----------------------

    /*JNI的Hello World*/
    public native String stringFromJNI();



}
