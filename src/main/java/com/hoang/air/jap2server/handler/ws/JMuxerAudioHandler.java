package com.hoang.air.jap2server.handler.ws;


import com.hoang.air.jap2lib.AirplayAudio;
import com.hoang.air.jap2lib.rtsp.AudioStreamInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@ChannelHandler.Sharable
public class JMuxerAudioHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> implements AirplayAudio {

    private static final Logger log = LogManager.getLogger(JMuxerAudioHandler.class);
    private static final int THRESHOLD = 32 * 1024;
    private final Set<ChannelHandlerContext> clients = ConcurrentHashMap.newKeySet(10);
    private ByteBuf send = ByteBufAllocator.DEFAULT.buffer();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) {
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.clients.add(ctx);
        log.info("WebSocket client connected: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        this.clients.remove(ctx);
        log.info("WebSocket client disconnected: {}", ctx.channel().remoteAddress());
    }

    private void sendData(ByteBuf message) {
        ByteBuf copy = message.retainedDuplicate();
        clients.forEach(client -> client.executor().execute(() -> client.writeAndFlush(new BinaryWebSocketFrame(copy))));

    }

    @Override
    public void onAudio(byte[] audio) {
        this.send.writeBytes(audio);
        if (send.readableBytes() >= THRESHOLD) {
            sendData(this.send);
            this.send.release();
            this.send = ByteBufAllocator.DEFAULT.buffer();
        }
    }

    @Override
    public void onAudioFormat(AudioStreamInfo audioInfo) {
        log.info("AudioStreamInfo getAudioFormat {}", audioInfo.getAudioFormat().toString());
        log.info("AudioStreamInfo getStreamType  {}", audioInfo.getStreamType().toString());
        log.info("AudioStreamInfo CompressionType{}", audioInfo.getCompressionType().toString());
    }
}