package org.elbek.chord.core;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;

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
        this.id = new BigInteger(1, HashUtil.SHA1(String.format("%s:%d", host, port))).mod(RingHelper.modulo);
        ;
    }

    public static ReferenceNode fromByte(byte[] bytes) {
        String s = new String(bytes);
        return new ReferenceNode(s.substring(0, s.indexOf(':')), Integer.parseInt(s.substring(s.indexOf(':') + 1)));
    }

    public static ReferenceNode read(ByteArray byteArray) throws IOException {
        int len = ByteReadWriter.readInt(byteArray);
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

    public Socket newSocket() throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port));
        return socket;
    }

    /**
     * returns true if this node is equal to currently running node
     *
     * @return
     */
    public boolean isSelfNode(Node node) {
        return node.self.equals(this);
    }
}
