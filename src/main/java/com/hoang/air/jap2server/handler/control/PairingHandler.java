package com.hoang.air.jap2server.handler.control;


import com.hoang.air.jap2server.handler.session.Session;
import com.hoang.air.jap2server.handler.session.SessionManager;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

@ChannelHandler.Sharable
public class PairingHandler extends ControlHandler {

    public PairingHandler(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    protected boolean handleRequest(ChannelHandlerContext ctx, Session session, FullHttpRequest request) throws Exception {
        var uri = request.uri();
        switch (uri) {
            case "/info": {
                var response = createResponseForRequest(request);
                session.getAirPlay().info(
                        new ByteBufOutputStream(response.content())
                );
                return sendResponse(ctx, request, response);
            }
            case "/pair-setup": {
                var response = createResponseForRequest(request);
                session.getAirPlay().pairSetup(
                        new ByteBufOutputStream(response.content())
                );
                return sendResponse(ctx, request, response);
            }
            case "/pair-verify": {
                var response = createResponseForRequest(request);
                session.getAirPlay().pairVerify(
                        new ByteBufInputStream(request.content()),
                        new ByteBufOutputStream(response.content())
                );
                return sendResponse(ctx, request, response);
            }
        }
        return false;
    }
}
