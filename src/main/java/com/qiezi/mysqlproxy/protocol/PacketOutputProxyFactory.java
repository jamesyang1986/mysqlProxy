package com.qiezi.mysqlproxy.protocol;

import java.io.OutputStream;

public class PacketOutputProxyFactory {

    public  static IPacketOutputProxy createProxy(OutputStream out) {
        return new RawPacketStreamOutputProxy(out);
    }
}
