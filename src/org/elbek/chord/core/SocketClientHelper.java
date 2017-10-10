package org.elbek.chord.core;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by elbek on 9/11/17.
 */
public class SocketClientHelper {

    /**
     * message format:
     * [1 byte keep alive flag][1 byte host data info run len][host data info run len bytes][1 byte task id][4 bytes message run len][message run len bytes messages]
     *
     * @param tasks
     * @param message
     * @param socket
     * @param keepAlive
     * @throws IOException
     */
    static void sentMessage(TaskRunner.TASKS tasks, byte[] message, Socket socket, boolean keepAlive) throws IOException {
        ByteArray byteArray = new ByteArray(100);
        byteArray.write(keepAlive ? 1 : 2);
        byte[] hostData = NodeStarter.systemNode.self.toByte();
        byteArray.write(hostData.length); //can send max of 255 bytes for hostData
        byteArray.write(hostData);
        byteArray.write(tasks.getTaskId());
        ByteReadWriter.writeInt(byteArray, message.length);
        byteArray.write(message);
        Logger.debug("sending task :" + tasks.name() + " to " + String.format("%s:%d", socket.getInetAddress().getHostName(), socket.getPort()));

        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        dos.writeInt(byteArray.size());
        dos.write(byteArray.getBytes(), 0, byteArray.size());
        dos.flush();
    }
}
