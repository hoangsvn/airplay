package com.hoang.air.jap2server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class HttpStaticServer implements Runnable {

    private static final Logger log = LogManager.getLogger(HttpStaticServer.class);
    private final int port;
    private final SimpleChannelInboundHandler<?> handler;

    public HttpStaticServer(int port, SimpleChannelInboundHandler<?> baseDir) {
        this.port = port;
        this.handler = baseDir;
    }


    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ChannelPipeline p = ch.pipeline();
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));
                            ch.pipeline().addLast(new ChunkedWriteHandler());
                            p.addLast(handler);
                        }
                    });

            Channel ch = b.bind(port).sync().channel();
            log.info("Server started: http://localhost:{}/", port);
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}



