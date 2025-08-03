package com.hoang.air.jap2server;


import com.hoang.air.jap2lib.utils.Nio;
import com.hoang.air.jap2server.handler.audio.AudioHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.DatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class AudioReceiver implements Runnable {


    private final AudioHandler audioHandler;
    private final Object monitor;

    private int port;

    public AudioReceiver(AudioHandler audioHandler, Object monitor) {
        this.audioHandler = audioHandler;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        var bootstrap = new Bootstrap();
        var workerGroup = Nio.EventLoopGroup();

        try {
            bootstrap.group(workerGroup)
                    .channel(Nio.DatagramChannel())
                    .localAddress(new InetSocketAddress(0))
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        public void initChannel(final DatagramChannel ch) {
                            ch.pipeline().addLast(audioHandler);
                        }
                    });
            var channelFuture = bootstrap.bind(0).sync();

            log.info("Audio receiver listening on port: {}", port = ((InetSocketAddress) channelFuture.channel().localAddress()).getPort());

            synchronized (monitor) {
                monitor.notify();
            }

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.info("Audio receiver interrupted");
        } finally {
            log.info("Audio receiver stopped");
            workerGroup.shutdownGracefully();
        }
    }

    public int getPort() {
        return port;
    }


}
