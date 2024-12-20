package com.qiezi.mysqlproxy.protocol;

import com.qiezi.mysqlproxy.server.FrontendConnection;

public interface IPacketOutputProxy {
    public static final int MIN_COMPRESS_LENGTH = 50;
    // 16M
    // http://dev.mysql.com/doc/internals/en/example-several-mysql-packets.html
    public static final int MAX_ORIG_CONTENT_LENGTH = (1 << 24) - 5;

    FrontendConnection getConnection();


    void write(byte b);

    void writeUB2(int i);

    void writeUB3(int i);

    void writeInt(int i);

    void writeFloat(float f);

    void writeUB4(long l);

    void writeLong(long l);

    void writeDouble(double d);

    void writeLength(long l);

    void write(byte[] src);

    void write(byte[] src, int off, int len);

    void writeWithNull(byte[] src);

    void writeWithLength(byte[] src);

    void writeWithLength(byte[] src, byte nullValue);

    int getLength(long length);

    int getLength(byte[] src);

    /**
     * 检查确保有输出空间并输出,所有的分配buffer操作都隐藏在内部,
     * 这里涉及到新分配buffer的情况，而且不能直接输出，所以依赖压缩和非压缩情况具体实现
     */
    void checkWriteCapacity(int capacity);

    /**
     * 用来标识当前packet的起始点，因为存在复合packet，
     * 所以对于同一个IPacketOutputProxy的多次begin会增加调用深度记录，
     * 对应当深度为0时的packetEnd调用才会导致数据真正被发送
     */
    void packetBegin();

    /**
     * 通知packet的边界，对于每个复合packet可以通知有多个边界， 这里决定最终如何输出
     * 每次必须发送完整的packet，否则会导致当前如果没有凑满另一个packet而之间的packet
     * 没有完整被发送，进而接收端收到不完整packet而处理错误
     * 对于复合packet，只标记完整的结束位置，否则同上，发送的子packet也无法被接收端正常处理
     */
    void packetEnd();

    /**
     * 当前的buffer是否有效
     */
    boolean avaliable();

    /**
     * 直接把整个byte[]当成完整的packet发出
     */
    void writeArrayAsPacket(byte[] src);

    void close();

    byte[] getData();
}
