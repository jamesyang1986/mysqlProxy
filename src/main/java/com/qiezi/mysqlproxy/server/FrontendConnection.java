package com.qiezi.mysqlproxy.server;

import com.qiezi.mysqlproxy.protocol.Capabilities;
import com.qiezi.mysqlproxy.protocol.PacketStreamOutputProxy;
import com.qiezi.mysqlproxy.protocol.packet.HandshakePacket;
import com.qiezi.mysqlproxy.utils.RandomUtil;
import com.qiezi.mysqlproxy.utils.StreamUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class FrontendConnection {
    private SocketChannel channel;
    private boolean isSsl = false;
    private boolean isAuth = false;
    private boolean readHeader = true;
    private Handler authHandler;

    private Handler queryHandler;

    private byte[] seed;

    private static final int PACKET_MAX_LEN = ((0xff << 16) | (0xff << 8) | 0xff);

    private static ByteBuffer header = ByteBuffer.allocate(4);


    public FrontendConnection(SocketChannel channel) {
        this.channel = channel;
        this.authHandler = new AuthHandler(this);
        this.queryHandler = new CommandHandler(this);
    }

    public void register(Selector selector) {
        try {
            SelectionKey key = this.channel.register(selector, SelectionKey.OP_READ);
            key.attach(this);
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        }
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
        if (!isAuth) {
            this.authHandler.handleData(data);
            return;
        }

        this.queryHandler.handleData(data);
    }

    private void handshake() {

        // 生成认证数据
        byte[] rand1 = RandomUtil.randomBytes(8);
        byte[] rand2 = RandomUtil.randomBytes(12);

        // 保存认证数据
        byte[] seed = new byte[rand1.length + rand2.length];
        System.arraycopy(rand1, 0, seed, 0, rand1.length);
        System.arraycopy(rand2, 0, seed, rand1.length, rand2.length);
        this.seed = seed;

        // 发送握手数据包
        HandshakePacket hs = new HandshakePacket();
        hs.packetId = 0;
        hs.protocolVersion = Versions.PROTOCOL_VERSION;
        hs.serverVersion = Versions.VERSION_PREFIX_5.getBytes();
        hs.threadId = Thread.currentThread().getId();
        hs.seed = rand1;
        hs.serverCapabilities = getServerCapabilities();
        hs.serverStatus = 2;
        hs.restOfScrambleBuff = rand2;

//        if (sslHandler != null) {
//            hs.serverCapabilities |= Capabilities.CLIENT_SSL;
//        }
        hs.serverCharsetIndex = (byte) (33 & 0xff);

        try {
            hs.write(new PacketStreamOutputProxy(this));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    protected int getServerCapabilities() {
        int flag = 0;
        flag |= Capabilities.CLIENT_LONG_PASSWORD;
        flag |= Capabilities.CLIENT_FOUND_ROWS;
        flag |= Capabilities.CLIENT_LONG_FLAG;
        flag |= Capabilities.CLIENT_CONNECT_WITH_DB;
        // flag |= Capabilities.CLIENT_NO_SCHEMA;
        flag |= Capabilities.CLIENT_COMPRESS;
        flag |= Capabilities.CLIENT_ODBC;
        // flag |= Capabilities.CLIENT_LOCAL_FILES;
        flag |= Capabilities.CLIENT_IGNORE_SPACE;
        flag |= Capabilities.CLIENT_PROTOCOL_41;
        flag |= Capabilities.CLIENT_INTERACTIVE;
        // flag |= Capabilities.CLIENT_SSL;
        flag |= Capabilities.CLIENT_IGNORE_SIGPIPE;
        flag |= Capabilities.CLIENT_TRANSACTIONS;
        // flag |= ServerDefs.CLIENT_RESERVED;
        flag |= Capabilities.CLIENT_SECURE_CONNECTION;

        // modified by chenghui.lch for
        flag |= Capabilities.CLIENT_MULTI_STATEMENTS;
        flag |= Capabilities.CLIENT_MULTI_RESULTS;
        // flag |= Capabilities.CLIENT_PS_MULTI_RESULTS;
        flag |= Capabilities.CLIENT_PLUGIN_AUTH;
//        if (DynamicConfig.getInstance().enableDeprecateEof()) {
//            flag |= Capabilities.CLIENT_DEPRECATE_EOF;
//        }
        return flag;
    }

    public int getPacketHeaderSize() {
        return 0;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public Handler getAuthHandler() {
        return authHandler;
    }

    public void setAuthHandler(Handler authHandler) {
        this.authHandler = authHandler;
    }

    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }
}
