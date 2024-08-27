package com.qiezi.mysqlproxy.protocol;

import com.qiezi.mysqlproxy.server.FrontendConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class PacketStreamOutputProxy extends PacketOutputProxyCommon {

    protected OutputStream out;

    protected ByteArrayOutputStream waitForCompressStream;

    private static int DEFAULT_BUFF_SIZE = 256;

    private FrontendConnection connection;

    public PacketStreamOutputProxy(OutputStream out) {
        this.out = out;
        this.waitForCompressStream = new ByteArrayOutputStream(DEFAULT_BUFF_SIZE);
    }

    public PacketStreamOutputProxy(FrontendConnection connection) {
        this.connection = connection;
        this.waitForCompressStream = new ByteArrayOutputStream(DEFAULT_BUFF_SIZE);
    }

    @Override
    public FrontendConnection getConnection() {
        return this.connection;
    }

    @Override
    public void write(byte b) {
        waitForCompressStream.write(b & 0xff);
    }

    @Override
    public void writeUB2(int i) {
        byte[] b = new byte[2];
        b[0] = (byte) (i & 0xff);
        b[1] = (byte) (i >>> 8);
        try {
            waitForCompressStream.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeUB3(int i) {
        byte[] b = new byte[3];
        b[0] = (byte) (i & 0xff);
        b[1] = (byte) (i >>> 8);
        b[2] = (byte) (i >>> 16);
        try {
            waitForCompressStream.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeInt(int i) {
        byte[] b = new byte[4];
        b[0] = (byte) (i & 0xff);
        b[1] = (byte) (i >>> 8);
        b[2] = (byte) (i >>> 16);
        b[3] = (byte) (i >>> 24);
        try {
            waitForCompressStream.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeFloat(float f) {
        writeInt(Float.floatToIntBits(f));
    }

    @Override
    public void writeUB4(long l) {
        byte[] b = new byte[4];
        b[0] = (byte) (l & 0xff);
        b[1] = (byte) (l >>> 8);
        b[2] = (byte) (l >>> 16);
        b[3] = (byte) (l >>> 24);
        try {
            waitForCompressStream.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeLong(long l) {
        byte[] b = new byte[8];
        b[0] = (byte) (l & 0xff);
        b[1] = (byte) (l >>> 8);
        b[2] = (byte) (l >>> 16);
        b[3] = (byte) (l >>> 24);
        b[4] = (byte) (l >>> 32);
        b[5] = (byte) (l >>> 40);
        b[6] = (byte) (l >>> 48);
        b[7] = (byte) (l >>> 56);
        try {
            waitForCompressStream.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeDouble(double d) {
        writeLong(Double.doubleToLongBits(d));
    }

    @Override
    public void writeLength(long l) {
        /**
         * l should be compared as unsigned long
         * refer: com.google.common.primitives.UnsignedLong.compare
         */
        long flipL = l ^ Long.MIN_VALUE;
        if (flipL < (251 ^ Long.MIN_VALUE)) {
            waitForCompressStream.write((byte) l);
        } else if (flipL < (0x10000L ^ Long.MIN_VALUE)) {
            waitForCompressStream.write((byte) 252);
            writeUB2((int) l);
        } else if (flipL < (0x1000000L ^ Long.MIN_VALUE)) {
            waitForCompressStream.write((byte) 253);
            writeUB3((int) l);
        } else {
            waitForCompressStream.write((byte) 254);
            writeLong(l);
        }
    }

    @Override
    public void write(byte[] src) {
        try {
            waitForCompressStream.write(src);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(byte[] src, int off, int len) {
        try {
            waitForCompressStream.write(src, off, len);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void writeWithNull(byte[] src) {
//        write(src);
        waitForCompressStream.write((byte) 0);
    }

    @Override
    public void writeWithLength(byte[] src) {
        int length = src.length;
        if (length < 251) {
            waitForCompressStream.write((byte) length);
        } else if (length < 0x10000L) {
            waitForCompressStream.write((byte) 252);
            writeUB2(length);
        } else if (length < 0x1000000L) {
            waitForCompressStream.write((byte) 253);
            writeUB3(length);
        } else {
            waitForCompressStream.write((byte) 254);
            writeLong(length);
        }
        write(src);
    }

    @Override
    public void writeWithLength(byte[] src, byte nullValue) {
        if (src == null) {
            write(nullValue);
        } else {
            writeWithLength(src);
        }
    }

    @Override
    public void checkWriteCapacity(int capacity) {

    }

    @Override
    public void packetBegin() {

    }

    @Override
    public void packetEnd() {
        try {
            if (this.connection != null) {
                this.connection.getChannel().write(ByteBuffer.wrap(getData()));
            } else if (out != null) {
                out.write(getData());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean avaliable() {
        /**
         * 对于流式输出，总假设可用
         */
        return true;
    }

    @Override
    public void close() {
        try {
            if (out != null) {
                out.close();
            }
            if (waitForCompressStream != null) {
                waitForCompressStream.close();
            }
        } catch (IOException e) {
        }
    }

    @Override
    public byte[] getData() {
        if (waitForCompressStream != null) {
            return waitForCompressStream.toByteArray();
        }
        return null;
    }
}

