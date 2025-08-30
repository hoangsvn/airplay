package com.hoang.air.handler.display;


import com.hoang.air.jap2lib.AirplayStream;
import com.hoang.air.jap2lib.rtsp.VideoStreamInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;


@ChannelHandler.Sharable
public class JMuxerVideoStream extends SimpleChannelInboundHandler<BinaryWebSocketFrame> implements AirplayStream {
    private static final Logger log = LoggerFactory.getLogger(JMuxerVideoStream.class);

    final Set<ChannelHandlerContext> clients = new HashSet<>();
    ByteArrayOutputStream temp = new ByteArrayOutputStream(32 * 1024);
    int THRESHOLD = 32 * 1024; // kb

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
                if (client.channel().isActive()) {
                    ByteBuf copy = message.retainedDuplicate();
                    client.writeAndFlush(new BinaryWebSocketFrame(copy))
                            .addListener(future -> {
                                if (!future.isSuccess()) {
                                    copy.release();
                                }
                            });
                }
            });
        }
    }

    @Override
    public void onVideo(byte[] video) {
        this.temp.writeBytes(video);
        if (temp.size() >= THRESHOLD) {
            ByteBuf buf = Unpooled.wrappedBuffer(temp.toByteArray());
            sendData(buf);
            buf.release();
            temp.reset();
        }
    }

    @Override
    public void onVideoFormat(VideoStreamInfo videoStreamInfo) {
        log.info("VideoStream StreamType :{}", videoStreamInfo.getStreamType().toString());
        log.info("VideoStream ConnectionID :{}", videoStreamInfo.getStreamConnectionID());
    }
}