package com.qiezi.mysqlproxy.server;

public interface Handler {
    public void handleData(byte[] data);
}
