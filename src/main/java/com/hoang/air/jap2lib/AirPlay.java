package com.hoang.air.jap2lib;


import com.hoang.air.jap2lib.fairplay.FairPlay;
import com.hoang.air.jap2lib.fairplay.FairPlayAudioDecryptor;
import com.hoang.air.jap2lib.fairplay.FairPlayVideoDecryptor;
import com.hoang.air.jap2lib.fairplay.Pairing;
import com.hoang.air.jap2lib.rtsp.MediaStreamInfo;
import com.hoang.air.jap2lib.rtsp.RTSP;

import java.io.InputStream;
import java.io.OutputStream;

public class AirPlay {

    private final Pairing pairing;
    private final FairPlay fairplay;
    private final RTSP rtsp;

    private FairPlayVideoDecryptor fairPlayVideoDecryptor;
    private FairPlayAudioDecryptor fairPlayAudioDecryptor;

    public AirPlay() {
        pairing = new Pairing();
        fairplay = new FairPlay();
        rtsp = new RTSP();
    }

    public void info(OutputStream out) throws Exception {
        pairing.info(out);
    }

    public void pairSetup(OutputStream out) throws Exception {
        pairing.pairSetup(out);
    }

    public void pairVerify(InputStream in, OutputStream out) throws Exception {
        pairing.pairVerify(in, out);
    }

    public boolean isPairVerified() {
        return pairing.isPairVerified();
    }

    public void fairPlaySetup(InputStream in, OutputStream out) throws Exception {
        fairplay.fairPlaySetup(in, out);
    }

    public MediaStreamInfo rtspGetMediaStreamInfo(InputStream in) throws Exception {
        return rtsp.getMediaStreamInfo(in);
    }

    public void rtspSetupEncryption(InputStream in) throws Exception {
        rtsp.setup(in);
    }

    public void rtspSetupVideo(OutputStream out, int videoDataPort, int videoEventPort, int videoTimingPort) throws Exception {
        rtsp.setupVideo(out, videoDataPort, videoEventPort, videoTimingPort);
    }

    public void rtspSetupAudio(OutputStream out, int audioDataPort, int audioControlPort) throws Exception {
        rtsp.setupAudio(out, audioDataPort, audioControlPort);
    }

    public byte[] getFairPlayAesKey() {
        return fairplay.decryptAesKey(rtsp.getEncryptedAESKey());
    }


    public boolean isFairPlayVideoDecryptorReady() {
        return pairing.getSharedSecret() != null && rtsp.getEncryptedAESKey() != null && rtsp.getStreamConnectionID() != null;
    }


    public boolean isFairPlayAudioDecryptorReady() {
        return pairing.getSharedSecret() != null && rtsp.getEncryptedAESKey() != null && rtsp.getEiv() != null;
    }

    public void decryptVideo(byte[] video) throws Exception {
        if (fairPlayVideoDecryptor == null) {
            if (!isFairPlayVideoDecryptorReady()) {
                throw new IllegalStateException("FairPlayVideoDecryptor not ready!");
            }
            fairPlayVideoDecryptor = new FairPlayVideoDecryptor(getFairPlayAesKey(), pairing.getSharedSecret(), rtsp.getStreamConnectionID());
        }
        fairPlayVideoDecryptor.decrypt(video);
    }

    public void decryptAudio(byte[] audio, int audioLength) throws Exception {
        if (fairPlayAudioDecryptor == null) {
            if (!isFairPlayAudioDecryptorReady()) {
                throw new IllegalStateException("FairPlayAudioDecryptor not ready!");
            }
            fairPlayAudioDecryptor = new FairPlayAudioDecryptor(getFairPlayAesKey(), rtsp.getEiv(), pairing.getSharedSecret());
        }
        fairPlayAudioDecryptor.decrypt(audio, audioLength);
    }
}
