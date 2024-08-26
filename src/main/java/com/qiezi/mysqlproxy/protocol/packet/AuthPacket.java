package com.qiezi.mysqlproxy.protocol.packet;

import com.qiezi.mysqlproxy.protocol.Capabilities;
import com.qiezi.mysqlproxy.protocol.IPacketOutputProxy;
import com.qiezi.mysqlproxy.protocol.MySQLMessage;
import com.qiezi.mysqlproxy.utils.BufferUtil;

import java.io.IOException;

public class AuthPacket extends MySQLPacket {

    private static final byte[] FILLER = new byte[23];

    public long clientFlags;
    public long maxPacketSize;
    public int charsetIndex;
    public byte[] extra;                // from FILLER(23)
    public String user;
    public byte[] password;
    public String database;
    public String authMethod;
    public boolean isSsl;

    public static boolean checkSsl(byte[] data) {
//        if (data.length == QuitPacket.QUIT.length && data[4] == Commands.COM_QUIT) {
//            return false;
//        }

        MySQLMessage mm = new MySQLMessage(data);
        mm.move(4);
        long clientFlags = mm.readUB4();
        if ((clientFlags & Capabilities.CLIENT_SSL) != 0) {
            // client use ssl
            return true;
        }

        return false;
    }

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        clientFlags = mm.readUB4();
        maxPacketSize = mm.readUB4();
        charsetIndex = (mm.read() & 0xff);
        // read extra
        int current = mm.position();
        int len = (int) mm.readLength();
        if (len > 0 && len < FILLER.length) {
            byte[] ab = new byte[len];
            System.arraycopy(mm.bytes(), mm.position(), ab, 0, len);
            this.extra = ab;
        }
        mm.position(current + FILLER.length);
        user = mm.readStringWithNull();
        password = mm.readBytesWithLength();
        if (((clientFlags & Capabilities.CLIENT_CONNECT_WITH_DB) != 0) && mm.hasRemaining()) {
            database = mm.readStringWithNull();
        }
        if (((clientFlags & Capabilities.CLIENT_PLUGIN_AUTH) != 0) && mm.hasRemaining()) {
            authMethod = mm.readStringWithNull();
        }
        if ((clientFlags & Capabilities.CLIENT_SSL) != 0) {
            // client use ssl
            isSsl = true;
        }
    }

    public IPacketOutputProxy write(IPacketOutputProxy proxy) throws IOException {
        // ------------------------
        proxy.packetBegin();

        proxy.writeUB3(getPacketLength());
        proxy.write(packetId);

        proxy.writeUB4(clientFlags);
        proxy.writeUB4(maxPacketSize);
        proxy.write((byte) charsetIndex);
        proxy.write(FILLER);
        if (user == null) {
            proxy.write((byte) 0);
        } else {
            proxy.writeWithNull(user.getBytes());
        }
        if (password == null) {
            proxy.write((byte) 0);
        } else {
            proxy.writeWithLength(password);
        }
        if (database == null) {
            proxy.write((byte) 0);
        } else {
            proxy.writeWithNull(database.getBytes());
        }

        proxy.packetEnd();
        // ------------------------
        return proxy;
    }

    protected int getPacketLength() {
        int size = 32;// 4+4+1+23;
        size += (user == null) ? 1 : user.length() + 1;
        size += (password == null) ? 1 : BufferUtil.getLength(password);
        size += (database == null) ? 1 : database.length() + 1;
        return size;
    }

    @Override
    protected String packetInfo() {
        return "MySQL Authentication Packet";
    }

}

