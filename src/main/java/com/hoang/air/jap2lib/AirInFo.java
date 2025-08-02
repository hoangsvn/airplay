package com.hoang.air.jap2lib;

public class AirInFo {
    private final String name;
    private final int airPlayPort;
    private final int airTunesPort;
    private final String mac;


    public AirInFo(String airPlayName, int airPlayPost, int airTunesPost, String mac) {
        this.name = airPlayName;
        this.airPlayPort = airPlayPost;
        this.airTunesPort = airTunesPost;
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public int getAirPlayPort() {
        return airPlayPort;
    }

    public int getAirTunesPort() {
        return airTunesPort;
    }

    public String getMac() {
        return mac;
    }
}
