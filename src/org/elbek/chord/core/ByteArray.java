package org.elbek.chord.core;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by elbek on 10/7/17.
 */
public class ByteArray {
    byte bytes[];
    int position = 0;

    public ByteArray(int size) {
        bytes = new byte[size];
    }

    public ByteArray(byte[] bytes) {
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

    static ByteArray from(InputStream input) throws IOException {
        DataInputStream dis = new DataInputStream(new BufferedInputStream(input));
        byte[] bytes = new byte[4];
        dis.readFully(bytes);
        ByteArray byteArray = new ByteArray(bytes);
        int len = ByteReadWriter.readInt(byteArray);
        bytes = new byte[len];
        dis.readFully(bytes);
        byteArray.setPosition(4); //since we already wrote 4 bytes
        byteArray.write(bytes);
        byteArray.setPosition(0); //set position to 0 to make it ready for read
        return byteArray;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "ByteArray{" +
                "bytes=" + Arrays.toString(bytes) +
                ", position=" + position +
                '}';
    }
}
