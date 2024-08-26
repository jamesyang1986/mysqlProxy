package com.qiezi.mysqlproxy.protocol;

import com.qiezi.mysqlproxy.server.FrontendConnection;

import java.io.IOException;
import java.io.OutputStream;

public class RawPacketStreamOutputProxy extends PacketStreamOutputProxy {

    public RawPacketStreamOutputProxy(OutputStream out) {
        super(out);
    }

    @Override
    public void checkWriteCapacity(int capacity) {
        /* 对于网络直接输出，不需要检查是否有空间，自动增长 */
    }

    @Override
    public FrontendConnection getConnection() {
        throw new UnsupportedOperationException("CompressPacketStreamOutputProxy not support getConnection");
    }

    @Override
    public void write(byte[] src) {
        try {
            out.write(src);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(byte[] src, int off, int len) {
        try {
            out.write(src, off, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void packetBegin() {

    }

    @Override
    public void packetEnd() {
        /**
         * 对于直接outputstream发送的情形不做特殊处理
         */
        try {
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getData() {
        return waitForCompressStream.toByteArray();
    }
}

