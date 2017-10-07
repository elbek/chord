package org.elbek.chord.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by elbek on 9/10/17.
 */
public class ByteReadWriter {

    public static void read(byte bytes[], InputStream input) throws IOException {
        for (int i = 0; i < bytes.length; i++) {
            int in = input.read();
            if (in == -1) {
                throw new EarlyBytesTerminatedException();
            }
            bytes[i] = (byte) in;
        }
    }

    public static class EarlyBytesTerminatedException extends IOException {

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

    public static void writeInt(OutputStream output, int i) throws IOException {
        output.write((byte)i>>>24);
        output.write((byte)i>>>16);
        output.write((byte)i>>>8);
        output.write((byte)i);
    }

}
