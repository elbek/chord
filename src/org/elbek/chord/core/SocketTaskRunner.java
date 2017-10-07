package org.elbek.chord.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * this class is wrapper for socket class that provides thread safe access with call back function
 * Created by elbek on 9/22/17.
 */
public class SocketTaskRunner implements Runnable {
    private ReferenceNode referenceNode;
    private int maxRetry = 2;
    private Socket socket;
    private volatile boolean stop = false;
    private BlockingDeque<SocketTask> tasks = new LinkedBlockingDeque<>();

    public SocketTaskRunner(Socket socket, ReferenceNode referenceNode) {
        this.socket = socket;
        this.referenceNode = referenceNode;
    }

    @Override
    public void run() {
        int currentTry = 0;
        boolean releaseLatch = true;
        while (true) {
            SocketTask socketTask = null;
            try {
                if (tasks.isEmpty() && Thread.currentThread().isInterrupted()) {
                    System.out.println("broken");
                    break;
                }
                socketTask = tasks.take();
                socketTask.doIt(socket);
                currentTry = 0;
                releaseLatch = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                stop = true;
            } catch (IOException e) {
                if (currentTry != maxRetry) {
                    try {
                        recover();
                        currentTry++;
                        tasks.add(socketTask);
                        releaseLatch = false;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        stop = true;
                    }
                } else {
                    stop = true;
                }
                e.printStackTrace();
            } finally {
                if (releaseLatch && socketTask != null && socketTask.countDownLatch != null) {
                    socketTask.countDownLatch.countDown();
                }
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
        }
    }

    private void recover() throws IOException {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        socket = new Socket();
        socket.connect(new InetSocketAddress(referenceNode.host, referenceNode.port));
    }

    public boolean add(SocketTask task) {
        if (stop) {
            return false;
        }
        tasks.add(task);
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SocketTaskRunner that = (SocketTaskRunner) o;

        return referenceNode.equals(that.referenceNode);
    }

    @Override
    public int hashCode() {
        return referenceNode.hashCode();
    }

    public synchronized void stop() {
        stop = true;
        Thread.currentThread().interrupt(); //in case queue is blocked waiting new socket commands
    }

    public Socket getSocket() {
        return socket;
    }

    public static abstract class SocketTask {
        CountDownLatch countDownLatch;

        public SocketTask() {
        }

        public SocketTask(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        abstract void doIt(Socket socket) throws IOException;
    }

    /**
     * this method tries to add socket task to socket task runner object of reference node
     * it repeats until it succeeds
     * @param node, system node
     * @param referenceNode, make sure this node is not node.self node.
     * @param socketTask task that runs in SocketTaskRunner
     */
    public static void add (Node node, ReferenceNode referenceNode, SocketTask socketTask) {
        assert referenceNode!=null && !referenceNode.equals(node.self) : "wrong reference used";
        SocketTaskRunner socketTaskRunner;
        do {
            socketTaskRunner = node.fingerTable.getSocketRunner(referenceNode);
            if (socketTaskRunner!=null && socketTaskRunner.add(socketTask)) {
                return;
            }

        } while (true);
    }
}