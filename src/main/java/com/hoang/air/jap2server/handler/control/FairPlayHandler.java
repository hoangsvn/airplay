package com.hoang.air.jap2server.handler.control;

import com.hoang.air.jap2server.handler.session.Session;
import com.hoang.air.jap2server.handler.session.SessionManager;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

@ChannelHandler.Sharable
public class FairPlayHandler extends ControlHandler {

    public FairPlayHandler(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    protected boolean handleRequest(ChannelHandlerContext ctx, Session session, FullHttpRequest request) throws Exception {
        var uri = request.uri();
        if ("/fp-setup".equals(uri)) {
            var response = createResponseForRequest(request);
            session.getAirPlay().fairPlaySetup(
                    new ByteBufInputStream(request.content()),
                    new ByteBufOutputStream(response.content())
            );
            return sendResponse(ctx, request, response);
        }
        return false;
    }
}
