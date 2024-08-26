package com.qiezi.mysqlproxy.protocol.packet;

import com.qiezi.mysqlproxy.protocol.IPacketOutputProxy;
import com.qiezi.mysqlproxy.protocol.MySQLMessage;

import java.io.IOException;

public class CommandPacket extends MySQLPacket {

    public byte command;
    public byte[] arg;

    public MySQLMessage read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        command = mm.read();
        arg = mm.readBytes();
        return mm;
    }

    public IPacketOutputProxy write(IPacketOutputProxy proxy) throws IOException {
        proxy.packetBegin();

        proxy.writeUB3(getPacketLength());
        proxy.write(packetId);

        proxy.write(command);
        proxy.write(arg);

        proxy.packetEnd();
        return proxy;
    }

    protected int getPacketLength() {
        return 1 + arg.length;
    }

    @Override
    protected String packetInfo() {
        return "MySQL Command Packet";
    }

}

