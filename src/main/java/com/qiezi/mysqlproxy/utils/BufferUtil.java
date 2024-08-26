package com.qiezi.mysqlproxy.utils;

import java.nio.ByteBuffer;

public class BufferUtil {

    public static final void writeUB3(ByteBuffer buffer, int i) {
        buffer.put((byte) (i & 0xff));
        buffer.put((byte) (i >>> 8));
        buffer.put((byte) (i >>> 16));
    }

    public static final int getLength(long l) {
        /**
         * l should be compared as unsigned long
         * refer: com.google.common.primitives.UnsignedLong.compare
         */
        l = l ^ Long.MIN_VALUE;
        if (l < (251 ^ Long.MIN_VALUE)) {
            return 1;
        } else if (l < (0x10000L ^ Long.MIN_VALUE)) {
            return 3;
        } else if (l < (0x1000000L ^ Long.MIN_VALUE)) {
            return 4;
        } else {
            return 9;
        }
    }

    public static final int getLength(byte[] src) {
        int length = src.length;
        if (length < 251) {
            return 1 + length;
        } else if (length < 0x10000L) {
            return 3 + length;
        } else if (length < 0x1000000L) {
            return 4 + length;
        } else {
            return 9 + length;
        }
    }

}
