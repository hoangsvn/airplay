package com.hoang.air.runner;


import com.hoang.air.jap2lib.AirInFo;
import com.hoang.air.jap2server.ControlServer;
import com.hoang.air.jap2server.handler.control.RTSPHandler;
import com.hoang.air.jap2server.handler.session.SessionManager;
import com.hoang.air.jap2server.handler.ws.JMuxerAudioHandler;
import com.hoang.air.jap2server.handler.ws.JMuxerStreamHandler;
import com.hoang.air.jap2server.handler.ws.WebSocketServer;
import com.hoang.air.jmdns.AirPlayBonjour;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AirPlayReceiver {

    AirPlayBonjour bonJour;

    @PostConstruct
    public void init() {

        AirInFo airInFo = new AirInFo("HoangPC", 8088, 8080, "01:02:03:04:05:06");
        bonJour = new AirPlayBonjour(airInFo);

        SessionManager seManager = new SessionManager();
        JMuxerStreamHandler video = new JMuxerStreamHandler();
        JMuxerAudioHandler audio = new JMuxerAudioHandler();
        RTSPHandler rtspHandler = new RTSPHandler(airInFo, seManager, video, audio);

        Thread airPlay = new Thread(new ControlServer(airInFo, seManager, rtspHandler), "air");
        Thread tvideo = new Thread(new WebSocketServer(8081, "/video", video), "video");
        Thread taudio = new Thread(new WebSocketServer(8082, "/audio", audio), "audio");
        try {
            bonJour.JmmDNSRegister();
            tvideo.start();
            taudio.start();
            airPlay.start();
            log.info("AirPlayReceiver started successfully");
        } catch (Exception e) {
            log.info("AirPlayReceiver failed to start");
        }
    }

    @PreDestroy
    public void destroy() {
        bonJour.JmmDNSUnRegister();
        log.info("AirPlayReceiver stopped");
    }
}
