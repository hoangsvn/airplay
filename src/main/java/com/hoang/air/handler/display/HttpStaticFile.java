package com.hoang.air.handler.display;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.io.InputStream;

@ChannelHandler.Sharable
public class HttpStaticFile extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String base;

    public HttpStaticFile(String base) {
        this.base = base;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String uri = request.uri();
        if (uri.startsWith("/") && uri.length() == 1) {
            redirect(ctx, "/index.html");
        }
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(base.concat(uri));

        if (inputStream == null) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        byte[] bytes = inputStream.readAllBytes();
        inputStream.close();

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.copiedBuffer(bytes));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, getContentType(uri));
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html";
        if (fileName.endsWith(".css")) return "text/css";
        if (fileName.endsWith(".js")) return "application/javascript";
        if (fileName.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Error".getBytes()));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void redirect(ChannelHandlerContext ctx, String url) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND); // 302
        response.headers().set(HttpHeaderNames.LOCATION, url);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}