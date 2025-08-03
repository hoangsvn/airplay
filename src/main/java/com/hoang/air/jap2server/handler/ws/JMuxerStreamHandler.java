package com.hoang.air.jap2server.handler.ws;


import com.hoang.air.jap2lib.AirplayStream;
import com.hoang.air.jap2lib.rtsp.VideoStreamInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;


@ChannelHandler.Sharable
public class JMuxerStreamHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> implements AirplayStream {

    Set<ChannelHandlerContext> clients = new HashSet<>();
    int THRESHOLD = 32 * 1024; // kb
    Logger log = LogManager.getLogger(JMuxerStreamHandler.class);
    ByteBuf send = ByteBufAllocator.DEFAULT.buffer();


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
        synchronized (clients) {
            clients.forEach(client -> {
                ByteBuf copy = message.retainedDuplicate();
                client.writeAndFlush(new BinaryWebSocketFrame(copy))
                        .addListener(future -> {
                            if (!future.isSuccess()) {
                                copy.release();
                            }
                        });
            });
        }
    }

    @Override
    public void onVideo(byte[] video) {
        this.send.writeBytes(video);
        if (send.readableBytes() >= THRESHOLD) {
            sendData(this.send);
            this.send.release();
            this.send = ByteBufAllocator.DEFAULT.buffer();
        }
    }

    @Override
    public void onVideoFormat(VideoStreamInfo videoStreamInfo) {
        log.info("VideoStreamInfo {}", videoStreamInfo.getStreamType().toString());
        log.info("VideoStreamInfo {}", videoStreamInfo.getStreamConnectionID());
    }
}