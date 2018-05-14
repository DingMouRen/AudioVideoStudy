package com.dingmouren.audiovideostudy.audio.exception;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class AudioConfigurationException extends Exception {
    public AudioConfigurationException(){
        super("录音器初始化配置失败");
    }
}
