package com.qiezi.mysqlproxy.protocol.packet;

import com.qiezi.mysqlproxy.protocol.IPacketOutputProxy;
import com.qiezi.mysqlproxy.utils.StreamUtil;

import java.io.IOException;
import java.io.InputStream;

public class BinaryPacket extends MySQLPacket {

    public static final byte OK = 1;
    public static final byte ERROR = 2;
    public static final byte HEADER = 3;
    public static final byte FIELD = 4;
    public static final byte FIELD_EOF = 5;
    public static final byte ROW = 6;
    public static final byte PACKET_EOF = 7;
    public static final byte LOCAL_INFILE = -5;
    public byte[] data;

    public void read(InputStream in) throws IOException {
        packetLength = StreamUtil.readUB3(in);
        packetId = StreamUtil.read(in);
        byte[] ab = new byte[packetLength];
        StreamUtil.read(in, ab, 0, ab.length);
        data = ab;
    }

    public IPacketOutputProxy write(IPacketOutputProxy proxy) {
        proxy.packetBegin();
        proxy.checkWriteCapacity(proxy.getConnection().getPacketHeaderSize());

        proxy.writeUB3(getPacketLength());
        proxy.write(packetId);

        proxy.write(data);
        proxy.packetEnd();
        return proxy;
    }

    private int getPacketLength() {
        return data == null ? 0 : data.length;
    }

    @Override
    protected String packetInfo() {
        return "MySQL Binary Packet";
    }

}

