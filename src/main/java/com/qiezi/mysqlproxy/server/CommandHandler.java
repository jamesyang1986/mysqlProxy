package com.qiezi.mysqlproxy.server;

public class CommandHandler implements Handler {
    private FrontendConnection source;

    public CommandHandler(FrontendConnection source) {
        this.source = source;
    }

    @Override
    public void handleData(byte[] data) {
        byte cmdType = data[0];
        switch (cmdType) {
            case 1:
                break;
            default:
                break;
        }

    }
}
