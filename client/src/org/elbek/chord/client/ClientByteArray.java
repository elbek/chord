package org.elbek.chord.client;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by elbek on 10/7/17.
 */
public class ClientByteArray {
    byte bytes[];
    int position = 0;

    public ClientByteArray(int size) {
        bytes = new byte[size];
    }

    public ClientByteArray(byte[] bytes) {
        this.bytes = bytes;
    }

    public void reset() {
        position = 0;
    }

    public int size() {
        return position;
    }

    public byte read(int index) {
         return bytes[index];
    }

    public void read(byte bytes[]) {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = read();
        }
    }

    public byte read() {
         return bytes[position++];
    }

    public void write(int b) {
        write((byte) b);
    }

    public void write(byte bytes[]) {
        for (byte aByte : bytes) {
            write(aByte);
        }
    }

    public void writeInt(int i) {
        write((byte)i>>>24);
        write((byte)i>>>16);
        write((byte)i>>>8);
        write((byte)i);
    }

    public void write(byte b) {
        if (position == bytes.length){
            byte newBytes[] = new byte[bytes.length*2];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            bytes = newBytes;
        }
        bytes[position++] = b;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
