package com.qiezi.mysqlproxy.server;

import com.qiezi.mysqlproxy.model.EndPoint;
import com.qiezi.mysqlproxy.protocol.Capabilities;
import com.qiezi.mysqlproxy.protocol.PacketStreamOutputProxy;
import com.qiezi.mysqlproxy.protocol.packet.*;
import com.qiezi.mysqlproxy.utils.SecurityUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class BackendConnection {
    private EndPoint target;

    private int connectTimeOut = 1000;

    private int readWriteTimeOut = 1000;

    private Socket socket;

    private InputStream in;
    private OutputStream out;

    private String userName;

    private String password;

    private long threadId;

    private int charsetIndex = 33;

    private boolean isAuth = false;

    private byte packetId = (byte) 0xff;
    private static final long MAX_PACKET_SIZE = 1024 * 1024 * 16;

    public BackendConnection(String host, int port, String userName, String password) {
        this.target = new EndPoint(host, port);
        this.userName = userName;
        this.password = password;
        connect();
        handshake();
    }

    public void executeSql(String sql) {
        CommandPacket packet = new CommandPacket();
        packet.packetId = this.packetId++;
        packet.command = MySQLPacket.COM_QUERY;
        try {
            packet.arg = sql.getBytes("utf-8");
            packet.write(new PacketStreamOutputProxy(out));
            BinaryPacket bin = receive();
            System.out.println(bin.packetId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void handshake() {
        try {
            BinaryPacket bin = new BinaryPacket();
            bin.read(this.in);
            HandshakePacket hsp = new HandshakePacket();
            hsp.read(bin);

            this.packetId = hsp.packetId;
            this.packetId++;


            this.threadId = hsp.threadId;
            // 发送认证数据包
            BinaryPacket res = null;
            try {
                res = sendAuth411(hsp);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
            switch (res.data[0]) {
                case OkPacket.OK_HEADER:
                    afterSuccess();
                    break;
                case ErrorPacket.ERROR_HEADER:
                    ErrorPacket err = new ErrorPacket();
                    err.read(bin);
                    throw new RuntimeException(new String(err.message, "utf-8"));
                default:
                    System.out.println("unknown command.");
                    break;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void connect() {
        try {
            this.socket = new Socket(this.target.getHost(), this.target.getPort());
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void afterSuccess() {
        isAuth = true;
    }

    private BinaryPacket sendAuth411(HandshakePacket hsp) throws IOException, NoSuchAlgorithmException {
        AuthPacket ap = new AuthPacket();
        ap.packetId = this.packetId++;
        ap.clientFlags = getClientFlags();
        ap.maxPacketSize = MAX_PACKET_SIZE;
        ap.charsetIndex = charsetIndex;
        ap.user = userName;
        String passwd = password;
        if (passwd != null && passwd.length() > 0) {
            byte[] password = passwd.getBytes("utf-8");
            byte[] seed = hsp.seed;
            byte[] restOfScramble = hsp.restOfScrambleBuff;
            byte[] authSeed = new byte[seed.length + restOfScramble.length];
            System.arraycopy(seed, 0, authSeed, 0, seed.length);
            System.arraycopy(restOfScramble, 0, authSeed, seed.length, restOfScramble.length);
            ap.password = SecurityUtil.scramble411(password, authSeed);
        }
        ap.write(new PacketStreamOutputProxy(out));
        return receive();
    }

    private BinaryPacket receive() {
        BinaryPacket bin = new BinaryPacket();
        try {
            bin.read(in);
            return bin;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static long getClientFlags() {
        int flag = 0;
        flag |= Capabilities.CLIENT_LONG_PASSWORD;
        flag |= Capabilities.CLIENT_FOUND_ROWS;
        flag |= Capabilities.CLIENT_LONG_FLAG;
        flag |= Capabilities.CLIENT_CONNECT_WITH_DB;
        // flag |= Capabilities.CLIENT_NO_SCHEMA;
        // flag |= Capabilities.CLIENT_COMPRESS;
        flag |= Capabilities.CLIENT_ODBC;
        // flag |= Capabilities.CLIENT_LOCAL_FILES;
        flag |= Capabilities.CLIENT_IGNORE_SPACE;
        flag |= Capabilities.CLIENT_PROTOCOL_41;
        flag |= Capabilities.CLIENT_INTERACTIVE;
        // flag |= Capabilities.CLIENT_SSL;
        flag |= Capabilities.CLIENT_IGNORE_SIGPIPE;
        flag |= Capabilities.CLIENT_TRANSACTIONS;
        // flag |= Capabilities.CLIENT_RESERVED;
        flag |= Capabilities.CLIENT_SECURE_CONNECTION;
        // client extension
        // flag |= Capabilities.CLIENT_MULTI_STATEMENTS;
        // flag |= Capabilities.CLIENT_MULTI_RESULTS;
        return flag;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static void main(String[] args) {
        BackendConnection connection = new BackendConnection("127.0.0.1", 3306, "root", "taotaoJJ1986@");
        connection.executeSql(" select * from test.cc ");
    }
}
