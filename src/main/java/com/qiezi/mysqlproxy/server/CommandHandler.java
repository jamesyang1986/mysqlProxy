package com.qiezi.mysqlproxy.server;

import com.qiezi.mysqlproxy.protocol.PacketStreamOutputProxy;
import com.qiezi.mysqlproxy.protocol.packet.*;

import java.util.List;

import static com.qiezi.mysqlproxy.protocol.packet.MySQLPacket.*;

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
            case COM_PING:
                break;
            case COM_QUIT:
                this.source.close();
                break;
            case COM_STMT_PREPARE:
                break;
            case COM_STMT_EXECUTE:
                break;
            case COM_STMT_CLOSE:
                break;
            case COM_QUERY:
                handleCmdQuery(data);
                break;
            default:
                break;
        }

    }


    private void handleCmdQuery(byte[] data) {
        try {
            this.source.packetId = 0x00;
            String sql = new String(data, "utf-8");
            System.out.println("query sql:" + sql);
            BackendConnection conn = this.source.getServer().getBackendConn();
            String sql2 = " select * from test.cc ";
            conn.executeSql(sql2);
            MySQLPacket packet = conn.readRsHeaderResult();

            if (packet == null) {
                //TODO
            }

            if (packet instanceof OkPacket) {
                OkPacket okPacket = (OkPacket) packet;
                okPacket.write(proxy);
            } else if (packet instanceof ErrorPacket) {
                ErrorPacket errorPacket = (ErrorPacket) packet;
                errorPacket.write(proxy);
            } else if (packet instanceof MysqlResultSetPacket) {
                handleResultSetPacket(conn, (MysqlResultSetPacket) packet);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void handleResultSetPacket(BackendConnection conn, MysqlResultSetPacket packet) throws Exception {
        MysqlResultSetPacket resultSetPacket = packet;
        List<RowDataPacket> rowDataPackets = conn.readRowDataResult(resultSetPacket.getResultHead().getFieldCount());

        if (resultSetPacket != null) {
            ResultSetHeaderPacket headerPacket = resultSetPacket.getResultHead();
            headerPacket.packetId = this.source.packetId++;
            resultSetPacket.getResultHead().write(proxy);
        }

        for (FieldPacket fieldPacket : resultSetPacket.getFieldPackets()) {
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
    }
}
