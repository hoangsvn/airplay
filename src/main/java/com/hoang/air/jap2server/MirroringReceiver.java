package com.hoang.air.jap2server;

import com.hoang.air.handler.mirroring.MirroringHandler;
import com.hoang.air.jap2lib.utils.Nio;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class MirroringReceiver implements Runnable {
    private final int port;
    private final MirroringHandler mirroring;
    ChannelFuture channelFuture;
    ServerBootstrap serverBootstrap;

    public MirroringReceiver(int port, MirroringHandler mirroring) {
        this.port = port;
        this.mirroring = mirroring;
    }

    @Override
    public void run() {
        serverBootstrap = new ServerBootstrap();
        var bossGroup = Nio.EventLoopGroup();
        var workerGroup = Nio.EventLoopGroup();
        try {
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(Nio.ServerSocketChannel())
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(final SocketChannel ch) {
                            ch.pipeline().addLast("mirroring", mirroring);
                        }
                    })
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            channelFuture = serverBootstrap.bind().sync();
            log.info("Mirroring receiver listening on port: {}", port);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.info("Mirroring receiver interrupted");
        } finally {
            log.info("Mirroring receiver stopped");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
