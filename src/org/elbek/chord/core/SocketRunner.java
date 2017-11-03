package org.elbek.chord.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * this class is wrapper for socket class that provides thread safe access with call back function thus enabling reuse of the same socket while making usage thread safe
 * Created by elbek on 9/22/17.
 */
public class SocketRunner implements Runnable {
    private ReferenceNode referenceNode;
    private Node node;
    private int maxRetry = 2;
    private Socket socket;
    private volatile boolean stop = false;
    private BlockingDeque<SocketTask> tasks = new LinkedBlockingDeque<>();

    //this creates new socket, thus creating new thread on the server side, create this object with care
    public SocketRunner(ReferenceNode referenceNode, Node node) throws IOException {
        this.socket = referenceNode.newSocket();
        this.referenceNode = referenceNode;
        this.node = node;
    }

    @Override
    public void run() {
        int currentTry = 0;
        boolean releaseLatch = true;
        while (true) {
            SocketTask socketTask = null;
            try {
                if (tasks.isEmpty() && Thread.currentThread().isInterrupted()) {
                    break;
                }
                socketTask = tasks.take();
                socketTask.doIt(socket, referenceNode);
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
                if (releaseLatch && socketTask != null && socketTask.latch != null) {
                    socketTask.latch.countDown();
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

    /**
     * add task to task queue
     * returns true if it was able to add, false if socket is already stopped
     * To add properly use {@link SocketRunner#add(Resolver, SocketTask)} method
     *
     * @param task
     * @return
     */
    public boolean add(SocketTask task) {
        if (stop) {
            return false;
        }
        tasks.add(task);
        return true;
    }

    @Override
    public String toString() {
        return "SocketRunner{" + referenceNode + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SocketRunner that = (SocketRunner) o;

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
        CountDownLatch latch;

        public SocketTask() {
        }

        public SocketTask(CountDownLatch latch) {
            this.latch = latch;
        }

        abstract void doIt(Socket socket, ReferenceNode referenceNode) throws IOException;
    }

    public interface Resolver {
        ReferenceNode resolve();
    }


    /**
     * this method tries to add socket task to socket task runner object of reference node
     * it repeats until it succeeds
     *
     * @param resolver,  the functor class to resolve Reference node
     * @param socketTask task that runs in SocketRunner
     */
    public static void add(Resolver resolver, SocketTask socketTask, Node node) throws IOException {
        SocketRunner socketRunner;
        do {
            ReferenceNode resolvedNode = resolver.resolve();
            if (resolvedNode == null) {
                continue;
            }
            if (resolvedNode.isSelfNode(node)) { //looks like no socket needed
                try {
                    socketTask.doIt(null, resolvedNode);
                } finally {
                    if (socketTask.latch != null) {
                        socketTask.latch.countDown();
                    }
                }
                break;
            }
            socketRunner = node.getSocketManager().getSocketRunner(resolvedNode);
            if (socketRunner != null && socketRunner.add(socketTask)) {
                return;
            }
        } while (true);
    }
}