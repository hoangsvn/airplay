package com.hoang.air.jap2server;

import com.hoang.air.jap2lib.utils.Nio;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class WebSocketServer implements Runnable {

    private final int port;
    private final SimpleChannelInboundHandler<?> handler;
    private final String path;

    public WebSocketServer(int port, String path, SimpleChannelInboundHandler<?> handler) {
        this.port = port;
        this.handler = handler;
        this.path = path;
    }
    
    @Override
    public void run() {
        ServerBootstrap webSocket = new ServerBootstrap();
        try {
            webSocket
                    .group(
                            Nio.EventLoopGroup(),
                            Nio.EventLoopGroup()
                    )
                    .channel(Nio.ServerSocketChannel())
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(final SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new HttpServerCodec(),
                                    new HttpObjectAggregator(65536),
                                    new WebSocketServerProtocolHandler(path),
                                    handler
                            );
                        }
                    });
            log.info("Starting websocket server in ws://localhost:{}{}", port, path);
            webSocket.bind().sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("InterruptedException websocket server", e);
        }
    }

}
