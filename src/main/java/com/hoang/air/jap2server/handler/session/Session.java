package com.hoang.air.jap2server.handler.session;


import com.hoang.air.jap2lib.AirPlay;

import java.util.Optional;

public class Session {

    private final AirPlay airPlay;
    private Thread airPlayReceiverThread;
    private Thread audioReceiverThread;
    private Thread audioControlServerThread;

    Session() {
        airPlay = new AirPlay();
    }

    public AirPlay getAirPlay() {
        return airPlay;
    }

    public void setAirPlayReceiverThread(Thread airPlayReceiverThread) {
        this.airPlayReceiverThread = airPlayReceiverThread;
    }

    public void setAudioReceiverThread(Thread audioReceiverThread) {
        this.audioReceiverThread = audioReceiverThread;
    }

    public void setAudioControlServerThread(Thread audioControlServerThread) {
        this.audioControlServerThread = audioControlServerThread;
    }

    public void stopMirroring() {
        Optional.ofNullable(airPlayReceiverThread).ifPresent(Thread::interrupt);
    }

    public void stopAudio() {
        Optional.ofNullable(audioReceiverThread).ifPresent(Thread::interrupt);
        Optional.ofNullable(audioControlServerThread).ifPresent(Thread::interrupt);
    }
}
