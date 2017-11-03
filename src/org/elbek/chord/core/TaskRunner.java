package org.elbek.chord.core;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by elbek on 9/10/17.
 */
public class TaskRunner implements Runnable {

    protected Socket clientSocket = null;
    Node node;

    public TaskRunner(Socket clientSocket, Node node) {
        this.clientSocket = clientSocket;
        this.node = node;
    }

    public void run() {
        InputStream input;
        boolean broken = false;
        DataOutputStream output = null;
        try {
            //clientSocket.setSoTimeout(1000 * 60 * 5);
            while (!broken) {
                input = clientSocket.getInputStream();
                DataInputStream dis = new DataInputStream(new BufferedInputStream(input));
                output = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
                int len;
                try {
                    len = dis.readInt();
                } catch (EOFException | SocketException e) {  //oo, client closed the socket, stop this thread
                    break;
                }
//                Logger.debug("message len came:" + len, node);
                byte bytes[] = new byte[len];
                dis.readFully(bytes);
                ByteArray byteArray = new ByteArray(bytes);
                int keepAliveFlag = byteArray.read();

                if (keepAliveFlag != 1) {
                    assert keepAliveFlag == 2 : "keeAliveFlag is invalid, got=" + keepAliveFlag;
                    broken = true;
                }
                int hostDataLen = byteArray.read();

                byte[] hostData = new byte[hostDataLen];
                byteArray.read(hostData);

                int task = byteArray.read();

                TASKS taskEnum = TASKS.find(task);
                assert taskEnum != null;
                Logger.debug(String.format("%s task came from %s", taskEnum.name(), new String(hostData)) + " len=" + len, node);

                switch (taskEnum) {
                    case OK:
                        String message = "OK";
                        output.writeInt(message.getBytes().length);
                        output.write(message.getBytes());
                        break;
                    case RETRIEVE_SUCCESSOR:
                        new SuccessorRetrieverTask(node).execute(byteArray, output);
                        break;
                    case SUCCESSOR_FINDER:
                        new SuccessorFinderTask(node).execute(byteArray, output);
                        break;

                    case PREDECESSOR_FINDER:
                        new PredecessorFinderTask(node).execute(byteArray, output);
                        break;

                    case PREDECESSOR_UPDATE:
                        new PredecessorUpdateTask(node).execute(byteArray, output);
                        break;

                    case JOIN:
                        new JoinTask(node).execute(byteArray, output);
                        break;
                    case STATE:
                        new StateTask(node).execute(byteArray, output);
                        break;
                    case RETRIEVE_PREDECESSOR:
                        new PredecessorRetrieverTask(node).execute(byteArray, output);
                        break;
                }
                output.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            //clientSocket.stop();
            //TODO
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignore) {
            }
        }
    }

    enum TASKS {
        OK(1),
        RETRIEVE_SUCCESSOR(2),
        SUCCESSOR_FINDER(3),
        PREDECESSOR_FINDER(4),
        PREDECESSOR_UPDATE(5),
        JOIN(6),
        STATE(7),
        RETRIEVE_PREDECESSOR(8);

        private int taskId;

        TASKS(int taskId) {
            this.taskId = taskId;
        }

        public static TASKS find(int taskId) {
            for (TASKS t : values()) {
                if (t.taskId == taskId) {
                    return t;
                }
            }
            return null;
        }

        public int getTaskId() {
            return taskId;
        }

    }
}