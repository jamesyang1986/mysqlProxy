package com.qiezi.mysqlproxy.config;

public class ServerConfig {
    private String listen;
    private int port;


    public String getListen() {
        return listen;
    }

    public void setListen(String listen) {
        this.listen = listen;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
