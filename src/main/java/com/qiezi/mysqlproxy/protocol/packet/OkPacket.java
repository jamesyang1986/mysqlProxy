package com.qiezi.mysqlproxy.protocol.packet;

import com.qiezi.mysqlproxy.protocol.IPacketOutputProxy;
import com.qiezi.mysqlproxy.protocol.MySQLMessage;
import com.qiezi.mysqlproxy.utils.BufferUtil;

public class OkPacket extends MySQLPacket {

    public static final byte OK_HEADER = 0x00;
    public static final byte EOF_HEADER = (byte) 0xFE;
    public static final byte[] OK = new byte[] {7, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0};
    public static final byte[] OK_WITH_MORE = new byte[] {7, 0, 0, 1, 0, 0, 0, 10, 0, 0, 0};

    public byte header;
    public long affectedRows;
    public long insertId;
    public int serverStatus;
    public int warningCount;
    public byte[] message;

    public OkPacket() {
        this(false);
    }

    public OkPacket(boolean isEof) {
        if (isEof) {
            this.header = EOF_HEADER;
        } else {
            this.header = OK_HEADER;
        }
    }

    public void read(BinaryPacket bin) {
        packetLength = bin.packetLength;
        packetId = bin.packetId;
        MySQLMessage mm = new MySQLMessage(bin.data);
        header = mm.read();
        affectedRows = mm.readLength();
        insertId = mm.readLength();
        serverStatus = mm.readUB2();
        warningCount = mm.readUB2();
        if (mm.hasRemaining()) {
            this.message = mm.readBytesWithLength();
        }
    }

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        header = mm.read();
        affectedRows = mm.readLength();
        insertId = mm.readLength();
        serverStatus = mm.readUB2();
        warningCount = mm.readUB2();
        if (mm.hasRemaining()) {
            this.message = mm.readBytesWithLength();
        }
    }

    public IPacketOutputProxy write(IPacketOutputProxy proxy) {
        proxy.packetBegin();

        proxy.writeUB3(getPacketLength());
        proxy.write(packetId);

        proxy.write(header);
        proxy.writeLength(affectedRows);
        proxy.writeLength(insertId);
        proxy.writeUB2(serverStatus);
        proxy.writeUB2(warningCount);
        if (message != null) {
            proxy.writeWithLength(message);
        }

        proxy.packetEnd();
        return proxy;
    }

    protected int getPacketLength() {
        int i = 1;
        i += BufferUtil.getLength(affectedRows);
        i += BufferUtil.getLength(insertId);
        i += 4;
        if (message != null) {
            i += BufferUtil.getLength(message);
        }
        return i;
    }

    @Override
    protected String packetInfo() {
        return "MySQL OK Packet";
    }

}