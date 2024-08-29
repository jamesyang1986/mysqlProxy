package com.qiezi.mysqlproxy.server;

import static com.qiezi.mysqlproxy.protocol.packet.MySQLPacket.COM_QUERY;

public class CommandHandler implements Handler {
    private FrontendConnection source;

    public CommandHandler(FrontendConnection source) {
        this.source = source;
    }

    @Override
    public void handleData(byte[] data) {
        byte cmdType = data[0];
        switch (cmdType) {
            case COM_QUERY:
                System.out.println(" this is query request.");
                break;
            default:
                break;
        }

    }
}
