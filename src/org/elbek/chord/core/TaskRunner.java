package org.elbek.chord.core;

import java.io.*;
import java.net.Socket;

/**
 * Created by elbek on 9/10/17.
 */
public class TaskRunner implements Runnable {

    protected Socket clientSocket = null;

    public TaskRunner(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        InputStream input = null;
        OutputStream output = null;
        try {
            //clientSocket.setSoTimeout(1000 * 60 * 5);
            input = clientSocket.getInputStream();
            DataInputStream dis = new DataInputStream(new BufferedInputStream(input));
            int len = dis.readInt();
            Logger.debug("message len came:" + len);
            byte bytes[] = new byte[len];
            dis.readFully(bytes);
            ByteArray byteArray = new ByteArray(bytes);
            int keepAliveFlag = byteArray.read();

            if (keepAliveFlag != 1) {
            }
            int hostDataLen = byteArray.read();

            byte[] hostData = new byte[hostDataLen];
            byteArray.read(hostData);

            int task = byteArray.read();
            output = clientSocket.getOutputStream();

            TASKS taskEnum = TASKS.find(task);
            assert taskEnum != null;
            Logger.debug(String.format("%s task came from %s", taskEnum.name(), new String(hostData)) + " len=" + len);

            switch (taskEnum) {
                case OK:
                    String message = "OK";
                    ByteReadWriter.writeInt(output, message.getBytes().length);
                    output.write(message.getBytes());
                    break;
                case RETRIEVE_SUCCESSOR:
                    new SuccessorRetrieverTask().execute(byteArray, output);
                    break;
                case SUCCESSOR_FINDER:
                    new SuccessorFinderTask().execute(byteArray, output);
                    break;

                case PREDECESSOR_FINDER:
                    new PredecessorFinderTask().execute(byteArray, output);
                    break;

                case PREDECESSOR_UPDATE:
                    new PredecessorUpdateTask().execute(byteArray, output);
                    break;

                case JOIN:
                    new JoinTask().execute(byteArray, output);
                    break;
                case STATE:
                    new StateTask().execute(byteArray, output);
                    break;
                case RETRIEVE_PREDECESSOR:
                    new PredecessorRetrieverTask().execute(byteArray, output);
                    break;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            //clientSocket.close();
            //TODO
        } finally {
            try {
                if (output != null) {
                    output.flush();
                }
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