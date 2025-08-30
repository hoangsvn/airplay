package com.hoang.air.jmdns;

import com.hoang.air.jap2lib.AirInFo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class AirPlayBonjour {
    private static final Logger log = LoggerFactory.getLogger(AirPlayBonjour.class);

    private static final String AIRPLAY_SERVICE_TYPE = "._airplay._tcp.local";
    private static final String AIRTUNES_SERVICE_TYPE = "._raop._tcp.local";

    private final AirInFo AirInfo;
    private final Thread TCP_AIRPLAY;
    private final Thread TCP_RAOP;
    private final JmmDNS JmDNS;
    private ServiceInfo mDns_AirPlay;
    private ServiceInfo mDns_Raop;


    public AirPlayBonjour(AirInFo in) {
        this.AirInfo = in;
        this.JmDNS = JmmDNS.Factory.getInstance();
        this.TCP_AIRPLAY = new Thread(() -> {
            mDns_AirPlay = ServiceInfo.create(
                    AirInfo.getName().concat(AIRPLAY_SERVICE_TYPE),
                    AirInfo.getName(),
                    AirInfo.getAirPlayPort(),
                    0, 0,
                    airPlayMDNSProps());
            try {
                JmDNS.registerService(mDns_AirPlay);
                log.info("Service is registered : {} port : {}", AirInfo.getName().concat(AIRPLAY_SERVICE_TYPE), AirInfo.getAirPlayPort());
                log.info("Service is HostNames : {}", String.join(" | ", JmDNS.getHostNames()));
            } catch (IOException e) {
                log.error("Service IOException", e);
            }
        }, "tcp-airplay");
        this.TCP_RAOP = new Thread(() -> {
            String airTunesServerName = "010203040506@" + AirInfo.getName();
            mDns_Raop = ServiceInfo.create(
                    airTunesServerName.concat(AIRTUNES_SERVICE_TYPE),
                    airTunesServerName,
                    AirInfo.getAirTunesPort(),
                    0, 0,
                    airTunesMDNSProps());
            try {
                JmDNS.registerService(mDns_Raop);
                log.info("Service is registered : {} port: {}", airTunesServerName + AIRTUNES_SERVICE_TYPE, AirInfo.getAirTunesPort());
            } catch (IOException e) {
                log.error("Service IOException", e);
            }

        }, "tcp-raop");
    }


    public void JmmDNSRegister() {
        TCP_AIRPLAY.start();
        TCP_RAOP.start();
    }

    public void JmmDNSUnRegister() {
        this.JmDNS.unregisterService(mDns_AirPlay);
        log.info("{} service is unregistered", mDns_AirPlay.getName());
        this.JmDNS.unregisterService(mDns_Raop);
        log.info("{} service is unregistered", mDns_Raop.getName());
    }

    private Map<String, String> airPlayMDNSProps() {
        HashMap<String, String> airPlayMDNSProps = new HashMap<>();
        airPlayMDNSProps.put("deviceid", "01:02:03:04:05:06");
        airPlayMDNSProps.put("features", "0x5A7FFFF7");
        airPlayMDNSProps.put("srcvers", "220.68");
        airPlayMDNSProps.put("flags", "0x4");
        airPlayMDNSProps.put("vv", "2");
        airPlayMDNSProps.put("model", "AppleTV2,1");
        airPlayMDNSProps.put("rhd", "5.6.0.0");
        airPlayMDNSProps.put("pw", "false");
        airPlayMDNSProps.put("pk", "b07727d6f6cd6e08b58ede525ec3cdeaa252ad9f683feb212ef8a205246554e7");
        airPlayMDNSProps.put("pi", "2e388006-13ba-4041-9a67-25dd4a43d536");
        return airPlayMDNSProps;
    }

    private Map<String, String> airTunesMDNSProps() {
        HashMap<String, String> airTunesMDNSProps = new HashMap<>();
        airTunesMDNSProps.put("ch", "2");
        airTunesMDNSProps.put("cn", "0,1,2,3");
        airTunesMDNSProps.put("da", "true");
        airTunesMDNSProps.put("et", "0,3,5");
        airTunesMDNSProps.put("vv", "2");
        airTunesMDNSProps.put("ft", "0x5A7FFFF7");
        airTunesMDNSProps.put("am", "AppleTV2,1");
        airTunesMDNSProps.put("md", "0,1,2");
        airTunesMDNSProps.put("rhd", "5.6.0.0");
        airTunesMDNSProps.put("pw", "false");
        airTunesMDNSProps.put("sr", "44100");
        airTunesMDNSProps.put("ss", "16");
        airTunesMDNSProps.put("sv", "false");
        airTunesMDNSProps.put("tp", "UDP");
        airTunesMDNSProps.put("txtvers", "1");
        airTunesMDNSProps.put("sf", "0x4");
        airTunesMDNSProps.put("vs", "220.68");
        airTunesMDNSProps.put("vn", "65537");
        airTunesMDNSProps.put("pk", "b07727d6f6cd6e08b58ede525ec3cdeaa252ad9f683feb212ef8a205246554e7");
        return airTunesMDNSProps;
    }
}
