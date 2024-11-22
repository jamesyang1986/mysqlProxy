package com.qiezi.mysqlproxy.protocol.packet;

import com.qiezi.mysqlproxy.protocol.IPacketOutputProxy;
import com.qiezi.mysqlproxy.protocol.MySQLMessage;
import com.qiezi.mysqlproxy.utils.BufferUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: jamesyang
 * @date: 2024/11/22
 */
public class RowDataPacket extends MySQLPacket {

    protected static final byte NULL_MARK = (byte) 251;

    public final int fieldCount;
    public final List<byte[]> fieldValues;

    public int size = -1;

    public RowDataPacket(int fieldCount) {
        this.fieldCount = fieldCount;
        this.fieldValues = new ArrayList<byte[]>(fieldCount);
    }

    public RowDataPacket(int fieldCount, byte[][] rowBytes1) {
        this.fieldCount = fieldCount;
        this.fieldValues = Arrays.asList(rowBytes1);
    }

    public void add(byte[] value) {
        fieldValues.add(value);
    }

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.length();
//        packetId = mm.read();
        for (int i = 0; i < fieldCount; i++) {
            fieldValues.add(mm.readBytesWithLength());
        }
    }

    public IPacketOutputProxy write(IPacketOutputProxy proxy) {
        proxy.packetBegin();

        proxy.checkWriteCapacity(proxy.getConnection().getPacketHeaderSize());
        proxy.writeUB3(getPacketLength());
        proxy.write(packetId);

        for (int i = 0; i < fieldCount; i++) {
            byte[] fv = fieldValues.get(i);
            if (fv == null) {
                proxy.checkWriteCapacity(1);
                proxy.write(RowDataPacket.NULL_MARK);
            } else {
                proxy.checkWriteCapacity(BufferUtil.getLength(fv.length));
                proxy.writeLength(fv.length);
                proxy.write(fv);
            }
        }

        proxy.packetEnd();
        return proxy;
    }

    protected int getPacketLength() {
        if (this.size != -1) {
            return this.size;
        }

        int size = 0;
        for (int i = 0; i < fieldCount; i++) {
            byte[] v = fieldValues.get(i);
            size += (v == null || v.length == 0) ? 1 : BufferUtil.getLength(v);
        }
        return size;
    }

    @Override
    protected String packetInfo() {
        return "MySQL RowData Packet";
    }

}

