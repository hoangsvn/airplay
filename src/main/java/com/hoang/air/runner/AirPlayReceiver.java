package com.hoang.air.runner;


import com.hoang.air.handler.control.RTSPHandler;
import com.hoang.air.handler.display.HttpStaticFile;
import com.hoang.air.handler.display.JMuxerAudioStream;
import com.hoang.air.handler.display.JMuxerVideoStream;
import com.hoang.air.handler.session.SessionManager;
import com.hoang.air.jap2lib.AirInFo;
import com.hoang.air.jap2server.ControlServer;
import com.hoang.air.jap2server.HttpStaticServer;
import com.hoang.air.jap2server.WebSocketServer;
import com.hoang.air.jmdns.AirPlayBonjour;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class AirPlayReceiver {
    private static final Logger log = LoggerFactory.getLogger(AirPlayReceiver.class);

    AirPlayBonjour bonJour;

    @PostConstruct
    public void init() {

        AirInFo AirInfo = new AirInFo("AirPlay-".concat(System.getProperty("user.name")), 9100, 9101, "01:02:03:04:05:06");
//        bonJour = new AirPlayBonjour(AirInfo);

        SessionManager managerS = new SessionManager();
        JMuxerVideoStream video = new JMuxerVideoStream();
        JMuxerAudioStream audio = new JMuxerAudioStream();

        HttpStaticFile http = new HttpStaticFile("static");

        new Thread(new ControlServer(AirInfo, managerS, new RTSPHandler(AirInfo, managerS, video, audio)), "air").start();
        new Thread(new WebSocketServer(9001, "/video", video), "video").start();
        new Thread(new WebSocketServer(9002, "/audio", audio), "audio").start();
        new Thread(new HttpStaticServer(9000, http), "http").start();

//        try {
//            bonJour.JmmDNSRegister();
//            log.info("AirPlayReceiver started successfully ");
//        } catch (Exception e) {
//            log.info("AirPlayReceiver failed to start");
//        }
    }

    @PreDestroy
    public void destroy() {
//        bonJour.JmmDNSUnRegister();
        log.info("AirPlayReceiver stopped");
    }
}
