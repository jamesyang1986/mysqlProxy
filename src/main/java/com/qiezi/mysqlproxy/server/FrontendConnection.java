package com.qiezi.mysqlproxy.server;

import com.qiezi.mysqlproxy.utils.StreamUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class FrontendConnection {
    private SocketChannel channel;
    private boolean isSsl = false;
    private boolean isAuth = false;
    private boolean readHeader = true;
    private Handler handler;

    private static final int PACKET_MAX_LEN = ((0xff << 16) | (0xff << 8) | 0xff);

    private static ByteBuffer header = ByteBuffer.allocate(4);

    public FrontendConnection(SocketChannel channel) {
        this.channel = channel;
        this.handler = new AuthHandler(this);
        handshake();
    }

    public void read() {
        try {
            int size = this.channel.read(header);
            if (size != 4) {
                System.out.println("fail to read header");
                return;
            }
            byte[] headerData = header.array();
            int len = StreamUtil.readUB3Len(headerData);
            byte sequenceId = headerData[3];
            if (len == PACKET_MAX_LEN) {

            }
            ByteBuffer body = ByteBuffer.allocate(len);
            this.channel.read(body);
            byte[] data = body.array();
            header.clear();
            handleData(data);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void handleData(byte[] data) {

    }

    private void handshake() {

    }

    public int getPacketHeaderSize() {
        return 0;
    }
}
