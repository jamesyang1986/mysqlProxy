package com.qiezi.mysqlproxy.protocol;

import com.qiezi.mysqlproxy.utils.ZlibUtil;

public abstract class PacketOutputProxyCommon implements IPacketOutputProxy {

    public interface CompressSpliter {

        void sendCompressPiece(byte[] content, byte sequenceId, int beforeLen);
    }

    public static boolean isValid(IPacketOutputProxy proxy) {
        return proxy != null && proxy.avaliable();
    }

    private byte sequenceId = 1; /*
     * sequenceId从1开始,
     * 因为支持多次分片压缩发送，所以需要记录sequenceId
     */

    protected void sequenceReset() {
        sequenceId = 1;
    }

    protected byte sequenceGetAndInc() {
        return sequenceId++;
    }

    protected void splitCompressAndSent(byte[] origContent, CompressSpliter spliter) {
        int remaing = origContent.length;
        int offset = 0;

        while (remaing > MAX_ORIG_CONTENT_LENGTH) {
            spliter.sendCompressPiece(ZlibUtil.compress(origContent, offset, MAX_ORIG_CONTENT_LENGTH),
                    sequenceId++,
                    MAX_ORIG_CONTENT_LENGTH);

            remaing -= MAX_ORIG_CONTENT_LENGTH;
            offset += MAX_ORIG_CONTENT_LENGTH;
        }

        if (remaing > 0) {
            spliter.sendCompressPiece(ZlibUtil.compress(origContent, offset, remaing), sequenceGetAndInc(), remaing);
        }
    }

    @Override
    public int getLength(long length) {
        if (length < 251) {
            return 1;
        } else if (length < 0x10000L) {
            return 3;
        } else if (length < 0x1000000L) {
            return 4;
        } else {
            return 9;
        }
    }

    @Override
    public int getLength(byte[] src) {
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

    @Override
    public void writeArrayAsPacket(byte[] src) {
        packetBegin();
        write(src);
        packetEnd();
    }
}

