package com.hoang.air.handler.display;


import com.hoang.air.handler.audio.AudioControlHandler;
import com.hoang.air.jap2lib.AirplayAudio;
import com.hoang.air.jap2lib.rtsp.AudioStreamInfo;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class JMuxerAudioStream extends SimpleChannelInboundHandler<BinaryWebSocketFrame> implements AirplayAudio {
    private static final Logger log = LoggerFactory.getLogger(JMuxerAudioStream.class);


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) {
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
    }


    @Override
    public void onAudio(byte[] audio) {
    }

    @Override
    public void onAudioFormat(AudioStreamInfo audioInfo) {
        log.info("AudioStreamInfo getAudioFormat {}", audioInfo.getAudioFormat().toString());
        log.info("AudioStreamInfo getStreamType  {}", audioInfo.getStreamType().toString());
        log.info("AudioStreamInfo CompressionType{}", audioInfo.getCompressionType().toString());
    }
}