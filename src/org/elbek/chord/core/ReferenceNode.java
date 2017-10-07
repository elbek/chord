package org.elbek.chord.core;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

/**
 * Created by elbek on 9/10/17.
 */
public class ReferenceNode {
    String host;
    int port;
    BigInteger id;

    public ReferenceNode(String host, int port) {
        this.host = host;
        this.port = port;
        this.id = new BigInteger(1, HashUtil.SHA1(String.format("%s:%d", host, port)));
        ;
    }

    public static ReferenceNode fromByte(byte[] bytes) {
        String s = new String(bytes);
        return new ReferenceNode(s.substring(0, s.indexOf(':')), Integer.parseInt(s.substring(s.indexOf(':') + 1)));
    }

    public static ReferenceNode read(ByteArray byteArray) throws IOException {
        int len = ByteReadWriter.readInt(byteArray);
        System.out.println(len);
        byte[] b = new byte[len];
        byteArray.read(b);
        return fromByte(b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferenceNode that = (ReferenceNode) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public byte[] toByte() {
        return String.format("%s:%d", host, port).getBytes();
    }

    @Override
    public String toString() {
        return "{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", id=" + id +
                '}';
    }
}
