package com.qiezi.mysqlproxy.protocol.packet;

import com.qiezi.mysqlproxy.protocol.IPacketOutputProxy;
import com.qiezi.mysqlproxy.protocol.MySQLMessage;

/**
 * @author: jamesyang
 * @date: 2024/11/22
 */
public class EOFPacket extends MySQLPacket {

    public static final int PACKET_LEN = 5; // 1+2+2;
    public static final byte EOF_HEADER = (byte) 0xfe;

    public byte header = EOF_HEADER;
    public int warningCount;
    public int status = SERVER_STATUS_AUTOCOMMIT;

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        header = mm.read();
        warningCount = mm.readUB2();
        status = mm.readUB2();
    }

    public IPacketOutputProxy write(IPacketOutputProxy proxy) {
        if (proxy.getConnection().isEofDeprecated()) {
            // Use Ok packet instead of EOF
            OkPacket ok = new OkPacket(true);
            ok.packetId = packetId;
            ok.serverStatus = status;
            ok.write(proxy);
            return proxy;
        }

        proxy.packetBegin();

        int size = getPacketLength();
        proxy.checkWriteCapacity(proxy.getConnection().getPacketHeaderSize() + size);
        proxy.writeUB3(size);
        proxy.write(packetId);

        proxy.write(header);
        proxy.writeUB2(warningCount);
        proxy.writeUB2(status);

        proxy.packetEnd();
        return proxy;
    }

    private int getPacketLength() {
        return PACKET_LEN;
    }

    @Override
    protected String packetInfo() {
        return "MySQL EOF Packet";
    }

}
