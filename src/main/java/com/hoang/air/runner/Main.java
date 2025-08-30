package com.hoang.air.runner;

import com.hoang.air.jap2lib.AirInFo;
import com.hoang.air.jmdns.AirPlayBonjour;

public class Main {
    public static void main(String[] args) {
        AirInFo AirInfo = new AirInFo("AirPlay-".concat(System.getProperty("user.name")), 9100, 9101, "01:02:03:04:05:06");
        AirPlayBonjour bonJour = new AirPlayBonjour(AirInfo);
        bonJour.JmmDNSRegister();
        
    }
}
