package com.hoang.air.jap2lib;


import com.hoang.air.jap2lib.rtsp.AudioStreamInfo;

public interface AirplayAudio {


    void onAudio(byte[] audio);

    void onAudioFormat(AudioStreamInfo audioInfo);
}
