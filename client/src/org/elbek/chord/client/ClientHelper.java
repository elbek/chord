package org.elbek.chord.client;

import java.io.*;
import java.net.Socket;

/**
 * Created by elbek on 9/11/17.
 */
public class ClientHelper {

    public enum TASKS {
        OK(1),
        RETRIEVE_SUCCESSOR(2),
        SUCCESSOR_FINDER(3),
        PREDECESSOR_FINDER(4),
        NOTIFY(5),
        JOIN(6),
        STATE(7);

        private int taskId;

        TASKS(int taskId) {
            this.taskId = taskId;
        }

        public int getTaskId() {
            return taskId;
        }

        public static TASKS find(int taskId) {
            for (TASKS t : values()) {
                if (t.taskId == taskId) {
                    return t;
                }
            }
            return null;
        }

    }

    public static void sentMessage(TASKS tasks, byte[] message, Socket socket, boolean keepAlive) throws IOException {
        ClientByteArray byteArray = new ClientByteArray(100);
        byteArray.write(keepAlive ? 1 : 2);
        String client = "client";
        byteArray.write(client.getBytes().length);
        byteArray.write(client.getBytes());
        byteArray.write(tasks.getTaskId());
        if (message != null) {
            byteArray.writeInt(message.length);
            byteArray.write(message);
        }
        System.out.println("sending task :" + tasks.name() + " to " + String.format("%s:%d", socket.getInetAddress().getHostName(), socket.getPort()) + " size " + byteArray.size());
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        out.writeInt(byteArray.size());
        out.write(byteArray.getBytes(), 0, byteArray.size());
        out.flush();
    }

    public static byte[] read(Socket socket) throws IOException {
        DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        int len = dis.readInt();
        byte[] b = new byte[len];
        dis.read(b); //TODO, we should check returned integer here, if -1 we should act right for this exception case
        return b;
    }

    public static void close(Socket socket) throws IOException {
        socket.getInputStream().close();
        socket.getOutputStream().close();
        socket.close();
    }
}
