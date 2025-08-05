package com.hoang.air.jap2server;


import com.hoang.air.handler.control.FairPlayHandler;
import com.hoang.air.handler.control.HeartBeatHandler;
import com.hoang.air.handler.control.PairingHandler;
import com.hoang.air.handler.control.RTSPHandler;
import com.hoang.air.handler.session.SessionManager;
import com.hoang.air.jap2lib.AirInFo;
import com.hoang.air.jap2lib.utils.Nio;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.rtsp.RtspDecoder;
import io.netty.handler.codec.rtsp.RtspEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ControlServer implements Runnable {


    private final PairingHandler pairingHandler;
    private final FairPlayHandler fairPlayHandler;
    private final RTSPHandler rtspHandler;
    private final HeartBeatHandler heartBeatHandler;

    private final AirInFo airInFo;

    public ControlServer(AirInFo airInFo, SessionManager sessionManager, RTSPHandler rtspHandler) {
        this.airInFo = airInFo;
        this.pairingHandler = new PairingHandler(sessionManager);
        this.fairPlayHandler = new FairPlayHandler(sessionManager);
        this.heartBeatHandler = new HeartBeatHandler(sessionManager);
        this.rtspHandler = rtspHandler;

    }


    @Override
    public void run() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        EventLoopGroup bossGroup = Nio.EventLoopGroup();
        EventLoopGroup workerGroup = Nio.EventLoopGroup();
        try {
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(Nio.ServerSocketChannel())
                    .localAddress(new InetSocketAddress(airInFo.getAirTunesPort()))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(final SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new RtspDecoder(),
                                    new RtspEncoder(),
                                    new HttpObjectAggregator(64 * 1024),
                                    pairingHandler,
                                    fairPlayHandler,
                                    rtspHandler,
                                    heartBeatHandler);
                        }
                    })
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            var channelFuture = serverBootstrap.bind().sync();
            log.info("Control server listening on port: {}", airInFo.getAirTunesPort());
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            log.info("Control server stopped");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
