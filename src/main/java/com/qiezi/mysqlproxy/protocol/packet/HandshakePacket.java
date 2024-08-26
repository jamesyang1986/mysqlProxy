package com.qiezi.mysqlproxy.protocol.packet;

import com.qiezi.mysqlproxy.protocol.Capabilities;
import com.qiezi.mysqlproxy.protocol.IPacketOutputProxy;
import com.qiezi.mysqlproxy.protocol.MySQLMessage;
import com.qiezi.mysqlproxy.utils.StreamUtil;

import java.nio.ByteBuffer;

public class HandshakePacket extends MySQLPacket {

    public byte protocolVersion;
    public byte[] serverVersion;
    public long threadId;
    public byte[] seed;
    public int serverCapabilities;
    public byte serverCharsetIndex;
    public int serverStatus;
    public byte[] restOfScrambleBuff;

    public static final String authMethod = "mysql_native_password";

    public void read(BinaryPacket bin) {
        packetLength = bin.packetLength;
        packetId = bin.packetId;
        MySQLMessage mm = new MySQLMessage(bin.data);
        protocolVersion = mm.read();
        serverVersion = mm.readBytesWithNull();
        threadId = mm.readUB4();
        seed = mm.readBytesWithNull();
        serverCapabilities = mm.readUB2(); // 读取Capability flag 的lower bytes
        serverCharsetIndex = mm.read();
        serverStatus = mm.readUB2();

        // modified by chenghui.lch
        serverCapabilities |= (mm.readUB2() << 16); // 读取Capability的upper bytes
        mm.move(11);
        // mm.move(13);

        restOfScrambleBuff = mm.readBytesWithNull();
    }

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        protocolVersion = mm.read();
        serverVersion = mm.readBytesWithNull();
        threadId = mm.readUB4();
        seed = mm.readBytesWithNull();
        serverCapabilities = mm.readUB2(); // 读取Capability flag 的lower bytes
        serverCharsetIndex = mm.read();
        serverStatus = mm.readUB2();

        // modified by chenghui.lch
        serverCapabilities |= (mm.readUB2() << 16); // 读取Capability的upper bytes
        mm.move(11);
        // mm.move(13);

        restOfScrambleBuff = mm.readBytesWithNull();
    }


    public IPacketOutputProxy write(IPacketOutputProxy proxy) {
        proxy.packetBegin();

        proxy.writeUB3(getPacketLength());
        proxy.write(packetId);

        proxy.write(protocolVersion);
        proxy.writeWithNull(serverVersion);
        proxy.writeUB4(threadId);
        proxy.writeWithNull(seed);
        proxy.writeUB2(serverCapabilities); // lower 2 bytes of
        // Capability Flags
        proxy.write(serverCharsetIndex);
        proxy.writeUB2(serverStatus);

        // modified by chenghui.lch by 2014.11.2
        // extend server Capability flag for CLIENT_MULTI_STATEMENTS,
        // CLIENT_MULTI_RESULTS and CLIENT_PS_MULTI_RESULTS
        proxy.writeUB2(serverCapabilities >>> 16); // upper 2 bytes
        // of Capability
        // Flags

        // buffer.position(buffer.position() + 13);
        // for (int i = 1; i <= 13; i++) {
        // buffer.put((byte) 0);
        // }
        if ((serverCapabilities & Capabilities.CLIENT_PLUGIN_AUTH) != 0) {
            proxy.write((byte) 21);
        } else {
            proxy.write((byte) 0);
        }
        for (int i = 1; i <= 10; i++) {
            proxy.write((byte) 0);
        }

        proxy.writeWithNull(restOfScrambleBuff);
        proxy.writeWithNull(authMethod.getBytes());

        proxy.packetEnd();
        return proxy;
    }

    private int getPacketLength() {
        int size = 1;
        size += serverVersion.length;// n
        size += 5;// 1+4
        size += seed.length;// 8
        size += 19;// 1+2+1+2+13
        size += restOfScrambleBuff.length;// 12
        size += 1;// 1
        size += authMethod.getBytes().length;
        size += 1;

        return size;
    }

    @Override
    protected String packetInfo() {
        return "MySQL Handshake Packet";
    }

}

