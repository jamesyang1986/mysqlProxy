package com.qiezi.mysqlproxy.server;

import com.qiezi.mysqlproxy.model.EndPoint;
import com.qiezi.mysqlproxy.utils.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class BackendConnection {
    private EndPoint target;
    private int connectTimeOut = 1000;
    private int readWriteTimeOut = 1000;

    private InputStream readStream;
    private OutputStream writeStream;

    public BackendConnection(String host, int port) {
        this.target = new EndPoint(host, port);
    }

    public void initConnect() {
        try {
            Socket socket = new Socket(this.target.getHost(), this.target.getPort());
            this.readStream = socket.getInputStream();
            this.writeStream = socket.getOutputStream();
            while (true) {
                int len = (int) StreamUtil.readLength(this.readStream);
                int packetId = (int) StreamUtil.readLength(this.readStream);
                byte[] data = new byte[len];
                this.readStream.read(data);
                if (data != null) {


                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
