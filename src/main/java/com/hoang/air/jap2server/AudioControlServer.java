package com.hoang.air.jap2server;

import com.hoang.air.jap2lib.utils.Nio;
import com.hoang.air.jap2server.handler.audio.AudioControlHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.DatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class AudioControlServer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(AudioControlServer.class);

    private final Object monitor;

    private int port;

    public AudioControlServer(Object monitor) {
        this.monitor = monitor;
    }

    @Override
    public void run() {
        var bootstrap = new Bootstrap();
        var workerGroup = Nio.EventLoopGroup();
        var audioControlHandler = new AudioControlHandler();

        try {
            bootstrap
                    .group(workerGroup)
                    .channel(Nio.DatagramChannel())
                    .localAddress(new InetSocketAddress(0)) // bind random port
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        public void initChannel(final DatagramChannel ch) {
                            ch.pipeline().addLast(audioControlHandler);
                        }
                    });

            var channelFuture = bootstrap.bind(0).sync();

            log.info("Audio control server listening on port: {}", port = ((InetSocketAddress) channelFuture.channel().localAddress()).getPort());
            synchronized (monitor) {
                monitor.notify();
            }

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.info("Audio control server interrupted");
        } finally {
            log.info("Audio control server stopped");
            workerGroup.shutdownGracefully();
        }
    }

    public int getPort() {
        return port;
    }
}