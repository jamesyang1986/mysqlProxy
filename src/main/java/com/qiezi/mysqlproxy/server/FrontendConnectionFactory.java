package com.qiezi.mysqlproxy.server;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FrontendConnectionFactory {
    private static Map<SocketChannel, FrontendConnection> connectionMap = new ConcurrentHashMap<>();

    public static FrontendConnection makeConnection(SocketChannel socketChannel) {
        if (!connectionMap.containsKey(socketChannel)) {
            synchronized (FrontendConnectionFactory.class) {
                if (!connectionMap.containsKey(socketChannel)) {
                    connectionMap.putIfAbsent(socketChannel, new FrontendConnection(socketChannel));
                }
            }
        }
        return connectionMap.get(socketChannel);
    }
}
