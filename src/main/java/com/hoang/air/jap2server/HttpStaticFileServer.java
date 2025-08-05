package com.hoang.air.jap2server;

import com.hoang.air.handler.display.HttpStaticFile;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpStaticFileServer {

    private final int port;
    private final String baseDir;

    public HttpStaticFileServer(int port, String baseDir) {
        this.port = port;
        this.baseDir = baseDir;
    }

    public static void main(String[] args) throws Exception {
        new HttpStaticFileServer(8080, "public").start(); // serve from ./public
    }

    public void start() throws Exception {
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
                            p.addLast(new HttpRequestDecoder());
                            p.addLast(new HttpObjectAggregator(65536));
                            p.addLast(new HttpResponseEncoder());
                            p.addLast(new ChunkedWriteHandler());
                            p.addLast(new HttpStaticFile(baseDir));
                        }
                    });

            Channel ch = b.bind(port).sync().channel();
            System.out.println("Server started: http://localhost:" + port + '/');
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

