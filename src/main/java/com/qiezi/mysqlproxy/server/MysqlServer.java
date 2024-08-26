package com.qiezi.mysqlproxy.server;

import com.qiezi.mysqlproxy.config.ServerConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MysqlServer {
    private ServerConfig serverConfig;

    private Selector selector;

    public MysqlServer(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public void start() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(serverConfig.getPort()));
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select(1000L);
                Set<SelectionKey> keySet = selector.selectedKeys();
                if (keySet == null || keySet.size() == 0)
                    continue;

                Iterator<SelectionKey> it = keySet.iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    if (key != null && key.isAcceptable()) {
                        ServerSocketChannel aa = (ServerSocketChannel) key.channel();
                        SocketChannel client = aa.accept();
                        client.configureBlocking(false);
                        Socket socket = client.socket();
                        socket.setTcpNoDelay(true);
                        socket.setSendBufferSize(4 * 1024);
                        socket.setReceiveBufferSize(4 * 1024);
                        socket.setKeepAlive(true);
                        socket.setSoTimeout(1000);
                        FrontendConnection connection = FrontendConnectionFactory
                                .makeConnection(client);
                        connection.register(selector);
                    } else if (key != null && key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
//                        FrontendConnection connection = FrontendConnectionFactory
//                                .makeConnection(channel);
//                        connection.read();

                        FrontendConnection connection = (FrontendConnection) key.attachment();
                        connection.read();
                    } else if (key != null && key.isWritable()) {

                    }

                    keySet.clear();
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public void shutDown() {


    }


    public static void main(String[] args) {
        ServerConfig config = new ServerConfig();
        config.setListen("0.0.0.0");
        config.setPort(3307);

        MysqlServer server = new MysqlServer(config);
        server.start();

    }
}
