package com.qiezi.mysqlproxy.server;

import com.qiezi.mysqlproxy.protocol.packet.AuthPacket;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AuthHandler implements Handler {
    public static final byte[] AUTH_OK = new byte[]{7, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0};
    private FrontendConnection source;

    public AuthHandler(FrontendConnection source) {
        this.source = source;
    }

    public void handleData(byte[] data) {
        AuthPacket authPacket = new AuthPacket();
        authPacket.read(data);
        try {
            this.source.getChannel().write(ByteBuffer.wrap(AUTH_OK));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        source.setHandler(new CommandHandler(source));
    }
}
