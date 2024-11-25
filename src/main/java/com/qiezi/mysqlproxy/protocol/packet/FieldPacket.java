package com.qiezi.mysqlproxy.protocol.packet;

import com.qiezi.mysqlproxy.protocol.IPacketOutputProxy;
import com.qiezi.mysqlproxy.protocol.MySQLMessage;
import com.qiezi.mysqlproxy.utils.BufferUtil;

/**
 * @author: jamesyang
 * @date: 2024/11/22
 */
public class FieldPacket extends MySQLPacket {
    public static final String DEFAULT_CATALOG_STR = "def";
    private static final byte[] DEFAULT_CATALOG = DEFAULT_CATALOG_STR.getBytes();
    private static final byte[] FILLER = new byte[]{0, 0};

    public byte[] catalog = DEFAULT_CATALOG;
    public byte[] db;
    public byte[] table;
    public byte[] orgTable;
    public byte[] name;
    public byte[] orgName;
    public int charsetIndex;
    public long length;
    public int type;
    public int flags;
    public byte decimals;
    public byte[] definition;
    // 未解包的数据
    public byte[] unpacked = null;
    public Object field = null;
    public boolean isFieldList = false;

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        this.packetLength = mm.readUB3();
        this.packetId = mm.read();
        this.catalog = mm.readBytesWithLength();
        this.db = mm.readBytesWithLength();
        this.table = mm.readBytesWithLength();
        this.orgTable = mm.readBytesWithLength();
        this.name = mm.readBytesWithLength();
        this.orgName = mm.readBytesWithLength();

        mm.move(1);
        this.charsetIndex = mm.readUB2();
        this.length = mm.readUB4();
        this.type = mm.read() & 0xff;
        this.flags = mm.readUB2();
        this.decimals = mm.read();
        mm.move(FILLER.length);
        if (mm.hasRemaining()) {
            this.definition = mm.readBytesWithLength();
        }
    }

    public IPacketOutputProxy write(IPacketOutputProxy proxy) {
        proxy.packetBegin();

        int size = getPacketLength();
        proxy.checkWriteCapacity(proxy.getConnection().getPacketHeaderSize() + size);
        proxy.writeUB3(size);
        proxy.write(packetId);
        byte nullVal = 0;
        if (this.unpacked != null) {
            proxy.write(unpacked);
        } else {
            proxy.writeWithLength(catalog, nullVal);
            proxy.writeWithLength(db, nullVal);
            proxy.writeWithLength(table, nullVal);
            proxy.writeWithLength(orgTable, nullVal);
            proxy.writeWithLength(name, nullVal);
            proxy.writeWithLength(orgName, nullVal);
            proxy.write((byte) 0x0C);
            proxy.writeUB2(charsetIndex);
            proxy.writeUB4(length);
            proxy.write((byte) (type & 0xff));
            proxy.writeUB2(flags);
            proxy.write(decimals);
            proxy.write((byte) 0x00);
            proxy.write((byte) 0x00);
            // buffer.position(buffer.position() + FILLER.length);
            if (definition != null) {
                proxy.writeWithLength(definition);
            }
        }

        proxy.packetEnd();
        return proxy;
    }

    private int getPacketLength() {
        // Object field = this.field;
        // "std".getBytes("GBK")
        if (this.unpacked != null) {
            return unpacked.length;
        } else {
            int size = (catalog == null ? 1 : BufferUtil.getLength(catalog));
            size += (db == null ? 1 : BufferUtil.getLength(db));
            size += (table == null ? 1 : BufferUtil.getLength(table));
            size += (orgTable == null ? 1 : BufferUtil.getLength(orgTable));
            size += (name == null ? 1 : BufferUtil.getLength(name));
            size += (orgName == null ? 1 : BufferUtil.getLength(orgName));
            size += 13;// 1+2+4+1+2+1+2
            if (definition != null) {
                size += BufferUtil.getLength(definition);
            }

            return size;
        }
    }

    public byte[] getName() {
        return name;
    }

    @Override
    protected String packetInfo() {
        return "MySQL Field Packet";
    }

}

