package com.qiezi.mysqlproxy.protocol.packet;

/**
 * @author: jamesyang
 * @date: 2024/11/22
 */
public class MysqlResultSetPacket extends MySQLPacket {
    public ResultSetHeaderPacket resultHead;
    public FieldPacket[] fieldPackets;

    public MysqlResultSetPacket(ResultSetHeaderPacket resultHead, FieldPacket[] fieldPackets) {
        this.resultHead = resultHead;
        this.fieldPackets = fieldPackets;
    }

    @Override
    protected String packetInfo() {
        return "ResultSet Packet";
    }

    public ResultSetHeaderPacket getResultHead() {
        return resultHead;
    }

    public FieldPacket[] getFieldPackets() {
        return fieldPackets;
    }
}
