package com.hoang.air.handler.control;


import com.hoang.air.handler.audio.AudioReceiverHandler;
import com.hoang.air.handler.mirroring.MirroringHandler;
import com.hoang.air.handler.session.Session;
import com.hoang.air.handler.session.SessionManager;
import com.hoang.air.jap2lib.AirInFo;
import com.hoang.air.jap2lib.AirplayAudio;
import com.hoang.air.jap2lib.AirplayStream;
import com.hoang.air.jap2lib.rtsp.AudioStreamInfo;
import com.hoang.air.jap2lib.rtsp.MediaStreamInfo;
import com.hoang.air.jap2lib.rtsp.VideoStreamInfo;
import com.hoang.air.jap2server.AudioControlServer;
import com.hoang.air.jap2server.AudioReceiver;
import com.hoang.air.jap2server.MirroringReceiver;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.rtsp.RtspMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class RTSPHandler extends ControlHandler {

    private static final Logger log = LoggerFactory.getLogger(RTSPHandler.class);

    private final AirplayStream video;
    private final AirInFo airInFo;
    private final AirplayAudio audio;


    public RTSPHandler(AirInFo airInFo, SessionManager sessionManager, AirplayStream stream, AirplayAudio audio) {
        super(sessionManager);
        this.video = stream;
        this.audio = audio;
        this.airInFo = airInFo;
    }

    @Override
    protected boolean handleRequest(ChannelHandlerContext ctx, Session session, FullHttpRequest request) throws Exception {
        var response = createResponseForRequest(request);
        if (RtspMethods.SETUP.equals(request.method())) {
            MediaStreamInfo mediaStreamInfo = session.getAirPlay().rtspGetMediaStreamInfo(new ByteBufInputStream(request.content()));
            if (mediaStreamInfo == null) {
                request.content().resetReaderIndex();
                session.getAirPlay().rtspSetupEncryption(new ByteBufInputStream(request.content()));
            } else {
                switch (mediaStreamInfo.getStreamType()) {
                    case AUDIO:
                        AudioStreamInfo audioStreamInfo = (AudioStreamInfo) mediaStreamInfo;
                        audio.onAudioFormat(audioStreamInfo);
                        var audioHandler = new AudioReceiverHandler(session.getAirPlay(), audio);
                        var audioReceiver = new AudioReceiver(audioHandler, this);
                        var audioReceiverThread = new Thread(audioReceiver);
                        session.setAudioReceiverThread(audioReceiverThread);
                        audioReceiverThread.start();
                        synchronized (this) {
                            wait();
                        }

                        var audioControlServer = new AudioControlServer(this);
                        var audioControlServerThread = new Thread(audioControlServer);
                        session.setAudioControlServerThread(audioControlServerThread);
                        audioControlServerThread.start();
                        synchronized (this) {
                            wait();
                        }

                        session.getAirPlay().rtspSetupAudio(
                                new ByteBufOutputStream(response.content()),
                                audioReceiver.getPort(),
                                audioControlServer.getPort()
                        );
                        break;

                    case VIDEO:
                        VideoStreamInfo videoStreamInfo = (VideoStreamInfo) mediaStreamInfo;
                        video.onVideoFormat(videoStreamInfo);
                        var mirroringHandler = new MirroringHandler(session.getAirPlay(), video);
                        var airPlayReceiver = new MirroringReceiver(airInFo.getAirPlayPort(), mirroringHandler);
                        var airPlayReceiverThread = new Thread(airPlayReceiver);
                        session.setAirPlayReceiverThread(airPlayReceiverThread);
                        airPlayReceiverThread.start();
                        session.getAirPlay().rtspSetupVideo(new ByteBufOutputStream(response.content()), airInFo.getAirPlayPort(), airInFo.getAirTunesPort(), 7011);
                        break;
                }
            }
            return sendResponse(ctx, request, response);
        } else if (RtspMethods.GET_PARAMETER.equals(request.method())) {
            byte[] content = "volume: 1.000000\r\n".getBytes(StandardCharsets.US_ASCII);
            response.content().writeBytes(content);
            return sendResponse(ctx, request, response);
        } else if (RtspMethods.RECORD.equals(request.method())) {
            response.headers().add("Audio-Latency", "11025");
            response.headers().add("Audio-Jack-Status", "connected; type=analog");
            return sendResponse(ctx, request, response);
        } else if (RtspMethods.SET_PARAMETER.equals(request.method())) {
            return sendResponse(ctx, request, response);
        } else if ("FLUSH".equals(request.method().toString())) {
            return sendResponse(ctx, request, response);
        } else if (RtspMethods.TEARDOWN.equals(request.method())) {
            MediaStreamInfo mediaStreamInfo = session.getAirPlay().rtspGetMediaStreamInfo(new ByteBufInputStream(request.content()));
            if (mediaStreamInfo != null) {
                switch (mediaStreamInfo.getStreamType()) {
                    case AUDIO:
                        session.stopAudio();
                        break;
                    case VIDEO:
                        session.stopMirroring();
                        break;
                }
            } else {
                session.stopAudio();
                session.stopMirroring();
            }
            return sendResponse(ctx, request, response);
        } else if ("POST".equals(request.method().toString()) && request.uri().equals("/audioMode")) {
            return sendResponse(ctx, request, response);
        }
        return false;
    }
}
