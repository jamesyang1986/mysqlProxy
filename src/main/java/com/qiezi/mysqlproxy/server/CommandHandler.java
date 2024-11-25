package com.qiezi.mysqlproxy.server;

import com.qiezi.mysqlproxy.protocol.PacketStreamOutputProxy;
import com.qiezi.mysqlproxy.protocol.packet.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static com.qiezi.mysqlproxy.protocol.packet.MySQLPacket.COM_QUERY;

public class CommandHandler implements Handler {
    private FrontendConnection source;
    private PacketStreamOutputProxy proxy;

    public CommandHandler(FrontendConnection source) {
        this.source = source;
        proxy = new PacketStreamOutputProxy(this.source);
    }

    @Override
    public void handleData(byte[] data) {
        byte cmdType = data[0];
        switch (cmdType) {
            case COM_QUERY:
                try {
                    this.source.packetId = 0x00;
                    String sql = new String(data, "utf-8");
                    System.out.println("query sql:" + sql);
                    BackendConnection conn = this.source.getServer().getBackendConn();
                    String sql2 = " select * from test.cc ";
                    conn.executeSql(sql2);
                    MysqlResultSetPacket mysqlResultSetPacket = conn.readRsHeaderResult();
                    List<RowDataPacket> rowDataPackets = conn.readRowDataResult(mysqlResultSetPacket.getResultHead().getFieldCount());

                    for (RowDataPacket dataPacket : rowDataPackets) {
                        System.out.println("-------row--------");
                        StringBuilder sb = new StringBuilder("");
                        for (byte[] cc : dataPacket.fieldValues) {
                            sb.append(new String(cc));
                            sb.append("----");
                        }
                        System.out.println(sb.toString());
                    }


                    if (mysqlResultSetPacket != null) {
                        ResultSetHeaderPacket headerPacket = mysqlResultSetPacket.getResultHead();
                        headerPacket.packetId = this.source.packetId++;
                        mysqlResultSetPacket.getResultHead().write(proxy);
                    }

                    for (FieldPacket fieldPacket : mysqlResultSetPacket.getFieldPackets()) {
                        fieldPacket.packetId = this.source.packetId++;
                        fieldPacket.write(proxy);
                    }
                    EOFPacket eofPacket = new EOFPacket();
                    eofPacket.packetId = this.source.packetId++;
                    eofPacket.write(proxy);

                    for (RowDataPacket dataPacket : rowDataPackets) {
                        dataPacket.packetId = this.source.packetId++;
                        dataPacket.write(proxy);
                    }

                    eofPacket = new EOFPacket();
                    eofPacket.packetId = this.source.packetId++;
                    eofPacket.write(proxy);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                break;
        }

    }
}
