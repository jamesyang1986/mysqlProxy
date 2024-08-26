package com.qiezi.mysqlproxy.server;

public class AuthHandler implements Handler {
    public static final byte[] AUTH_OK = new byte[]{7, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0};
    private FrontendConnection source;

    public  AuthHandler(FrontendConnection source){
        this.source = source;
    }
    public void handleData(byte[] data) {

    }
}
