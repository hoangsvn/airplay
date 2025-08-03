package com.hoang.air.jap2server.handler.audio;


import com.hoang.air.jap2lib.AirPlay;
import com.hoang.air.jap2lib.AirplayAudio;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class AudioHandler extends SimpleChannelInboundHandler<DatagramPacket> {


    private final AirPlay airPlay;
    private final AirplayAudio audio;

    private final AudioPacket[] buffer = new AudioPacket[512];

    private int prevSeqNum;


    public AudioHandler(AirPlay airPlay, AirplayAudio audio) {
        this.airPlay = airPlay;
        this.audio = audio;
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = new AudioPacket();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf content = msg.content();
        byte[] headerBytes = new byte[12];
        content.readBytes(headerBytes);
        int flag = headerBytes[0] & 0xFF;
        int type = headerBytes[1] & 0x7F;

        int curSeqNo = ((headerBytes[2] & 0xFF) << 8) | (headerBytes[3] & 0xFF);
        long timestamp = (headerBytes[7] & 0xFF) | ((headerBytes[6] & 0xFF) << 8) | ((headerBytes[5] & 0xFF) << 16) | ((long) (headerBytes[4] & 0xFF) << 24);
        long ssrc = (headerBytes[11] & 0xFF) | ((headerBytes[6] & 0xFF) << 8) | ((headerBytes[9] & 0xFF) << 16) | ((long) (headerBytes[8] & 0xFF) << 24);
        if (curSeqNo <= prevSeqNum) {
            return;
        }

        AudioPacket audioPacket = buffer[curSeqNo % buffer.length];
        audioPacket.flag(flag)
                .type(type)
                .sequenceNumber(curSeqNo)
                .timestamp(timestamp)
                .ssrc(ssrc)
                .available(true)
                .encodedAudioSize(content.readableBytes())
                .encodedAudio(packet -> content.readBytes(packet, 0, content.readableBytes()));
        while (dequeue(curSeqNo)) {
            curSeqNo++;
        }
    }

    private boolean dequeue(int curSeqNo) throws Exception {
        if (curSeqNo - prevSeqNum == 1 || prevSeqNum == 0) {
            AudioPacket audioPacket = buffer[curSeqNo % buffer.length];
            if (audioPacket.isAvailable()) {
                airPlay.decryptAudio(audioPacket.getEncodedAudio(), audioPacket.getEncodedAudioSize());
                audio.onAudio(Arrays.copyOfRange(audioPacket.getEncodedAudio(), 0, audioPacket.getEncodedAudioSize()));
                audioPacket.available(false);
                prevSeqNum = curSeqNo;
                return true;
            }
        }
        return false;
    }
}
