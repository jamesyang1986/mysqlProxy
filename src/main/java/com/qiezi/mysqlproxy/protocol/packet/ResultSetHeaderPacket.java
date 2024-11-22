package com.qiezi.mysqlproxy.protocol.packet;

import com.qiezi.mysqlproxy.protocol.IPacketOutputProxy;
import com.qiezi.mysqlproxy.utils.BufferUtil;

/**
 * @author: jamesyang
 * @date: 2024/11/22
 */
public class ResultSetHeaderPacket extends MySQLPacket {

    public int fieldCount;

    public ResultSetHeaderPacket(int fieldCount) {
        this.fieldCount = fieldCount;
    }

    public IPacketOutputProxy write(IPacketOutputProxy proxy) {
        proxy.packetBegin();

        int size = getPacketLength();
        proxy.checkWriteCapacity(proxy.getConnection().getPacketHeaderSize() + size);
        proxy.writeUB3(size);
        proxy.write(packetId);

        proxy.writeLength(fieldCount);

        proxy.packetEnd();
        return proxy;
    }

    private int getPacketLength() {
        return BufferUtil.getLength(fieldCount);
    }

    @Override
    protected String packetInfo() {
        return "MySQL ResultSetHeader Packet";
    }

    public int getFieldCount() {
        return fieldCount;
    }
}
