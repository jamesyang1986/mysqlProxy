package com.qiezi.mysqlproxy.protocol.packet;

import com.qiezi.mysqlproxy.protocol.IPacketOutputProxy;
import com.qiezi.mysqlproxy.protocol.MySQLMessage;

public class ErrorPacket extends MySQLPacket {

    public static final byte ERROR_HEADER = (byte) 0xff;
    private static final byte SQLSTATE_MARKER = (byte) '#';
    private static final byte[] DEFAULT_SQLSTATE = "HY000".getBytes();

    public byte fieldCount = ERROR_HEADER;
    public int errno;
    public byte mark = SQLSTATE_MARKER;
    public byte[] sqlState = DEFAULT_SQLSTATE;
    public byte[] message;

    public void read(BinaryPacket bin) {
        packetLength = bin.packetLength;
        packetId = bin.packetId;
        MySQLMessage mm = new MySQLMessage(bin.data);
        fieldCount = mm.read();
        errno = mm.readUB2();
        if (mm.hasRemaining() && (mm.read(mm.position()) == SQLSTATE_MARKER)) {
            mm.read();
            sqlState = mm.readBytes(5);
        }
        message = mm.readBytes();
    }

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        fieldCount = mm.read();
        errno = mm.readUB2();
        if (mm.hasRemaining() && (mm.read(mm.position()) == SQLSTATE_MARKER)) {
            mm.read();
            sqlState = mm.readBytes(5);
        }
        message = mm.readBytes();
    }

    public IPacketOutputProxy write(IPacketOutputProxy proxy) {
        proxy.packetBegin();

        int size = getPacketLength();

        proxy.checkWriteCapacity(proxy.getConnection().getPacketHeaderSize() + size);

        proxy.writeUB3(size);
        proxy.write(packetId);

        proxy.write(fieldCount);
        proxy.writeUB2(errno);
        proxy.write(mark);
        if (sqlState == null || sqlState.length == 0) {
            sqlState = DEFAULT_SQLSTATE;
        }
        proxy.write(sqlState);
        if (message != null) {
            proxy.write(message);
        }

        proxy.packetEnd();
        return proxy;
    }

    private int getPacketLength() {
        int size = 9;// 1 + 2 + 1 + 5
        if (message != null) {
            size += message.length;
        }
        return size;
    }

    @Override
    protected String packetInfo() {
        return "MySQL Error Packet";
    }

}

