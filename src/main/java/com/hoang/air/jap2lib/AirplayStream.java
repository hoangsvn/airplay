package com.hoang.air.jap2lib;


import com.hoang.air.jap2lib.rtsp.VideoStreamInfo;

public interface AirplayStream {

    void onVideo(byte[] video);

    void onVideoFormat(VideoStreamInfo videoStreamInfo);

}
