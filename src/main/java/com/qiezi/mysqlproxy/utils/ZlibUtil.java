package com.qiezi.mysqlproxy.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.*;

public class ZlibUtil {
    public static byte[] compress(byte[] data) {
        return compress(data, 0, data.length);
    }

    public static byte[] compress(byte[] data, int off, int pieceLen) {
        byte[] output = null;
        Deflater compressor = new Deflater();
        compressor.reset();
        compressor.setInput(data, off, pieceLen);
        compressor.finish();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(pieceLen);
        try {
            byte[] buf = new byte[1024];
            while (!compressor.finished()) {
                int len = compressor.deflate(buf);
                bos.write(buf, 0, len);
            }
            output = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
            }
        }

        compressor.end();
        return output;
    }

    public static byte[] decompress(byte[] data) {
        byte[] output = null;

        Inflater decompressor = new Inflater();
        decompressor.reset();
        decompressor.setInput(data);
        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompressor.finished()) {
                int len = decompressor.inflate(buf);
                o.write(buf, 0, len);
            }
            output = o.toByteArray();
        } catch (DataFormatException e) {
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompressor.end();
        return output;
    }

    public static void compress(byte[] data, OutputStream os) {
        compress(data, 0, data.length, os);
    }

    public static void compress(byte[] data, int start, int length, OutputStream os) {
        DeflaterOutputStream dos = new DeflaterOutputStream(os);
        try {
            dos.write(data, start, length);
            dos.finish();
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] decompress(InputStream is) {
        InflaterInputStream iis = new InflaterInputStream(is);
        ByteArrayOutputStream o = new ByteArrayOutputStream(1024);

        try {
            int len = 1024;
            byte[] buf = new byte[len];
            while ((len = iis.read(buf, 0, len)) > 0) {
                o.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return o.toByteArray();
    }
}
