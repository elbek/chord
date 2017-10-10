package org.elbek.chord.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by elbek on 9/10/17.
 */
public class ByteReadWriter {

    public static void read(byte bytes[], InputStream input) throws IOException {
        int in = input.read(bytes);
        if (in == -1) {
            throw new EarlyBytesTerminatedException();
        }
    }

    public static int readInt(InputStream input) throws IOException {
        byte[] b = new byte[4];
        read(b, input);
        int res = b[0] & 0xFF;
        res = res << 8 | (b[1] & 0xFF);
        res = res << 8 | (b[2] & 0xFF);
        res = res << 8 | (b[3] & 0xFF);
        return res;
    }

    public static int readInt(ByteArray byteArray) throws IOException {
        int res = byteArray.read() & 0xFF;
        res = res << 8 | (byteArray.read() & 0xFF);
        res = res << 8 | (byteArray.read() & 0xFF);
        res = res << 8 | (byteArray.read() & 0xFF);
        return res;
    }

    public static void writeInt(ByteArray byteArray, int i) throws IOException {
        byteArray.write((byte) (i >>> 24));
        byteArray.write((byte) (i >>> 16));
        byteArray.write((byte) (i >>> 8));
        byteArray.write((byte) i);
    }

    public static class EarlyBytesTerminatedException extends IOException {

    }

}
