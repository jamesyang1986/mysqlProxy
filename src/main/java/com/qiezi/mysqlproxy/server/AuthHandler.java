package com.qiezi.mysqlproxy.server;

import com.qiezi.mysqlproxy.protocol.IPacketOutputProxy;
import com.qiezi.mysqlproxy.protocol.PacketStreamOutputProxy;
import com.qiezi.mysqlproxy.protocol.packet.AuthPacket;
import com.qiezi.mysqlproxy.protocol.packet.OkPacket;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AuthHandler implements Handler {
    public static final byte[] AUTH_OK = new byte[]{7, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0};
    private FrontendConnection source;
    private IPacketOutputProxy proxy;

    public AuthHandler(FrontendConnection source) {
        this.source = source;
        proxy = new PacketStreamOutputProxy(source);
    }

    public void handleData(byte[] data) {
        AuthPacket authPacket = new AuthPacket();
        authPacket.read(data);
        byte cc = authPacket.packetId;
        if (cc != this.source.packetId) {
            System.out.println("error seq no");
        }

        try {
            OkPacket okPacket = new OkPacket();
            okPacket.packetId = this.source.packetId++;
            okPacket.write(proxy);
//            this.source.getChannel().write(ByteBuffer.wrap(AUTH_OK));
            source.setAuth(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
