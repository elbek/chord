package org.elbek.chord.core;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by elbek on 9/10/17.
 */
public class Node implements Runnable {
    BigInteger id;
    //todo, make this private and create getter for it, so none can modify outside of this class
    ReferenceNode predecessor;
    ReferenceNode self;
    FingerTable fingerTable;
    SocketRunnerManager socketManager = new SocketRunnerManager();
    private String host;
    private int port;
    private ServerSocket serverSocket = null;
    private boolean stopped = true;
    private ExecutorService threadPool = Executors.newFixedThreadPool(100);
    private boolean debug = false;

    public Node(NodeStarter nodeStarter) {
        this.port = nodeStarter.port;
        this.host = nodeStarter.host;
        debug = nodeStarter.debug;
        id = new BigInteger(1, HashUtil.SHA1(String.format("%s:%d", host, port)));
        self = new ReferenceNode(host, port);
        predecessor = self;
        fingerTable = FingerTable.build(this, self);
    }

    public void run() {
        if (!stopped) {
            System.out.println("Server already running");
            return;
        }
        openServerSocket();
        stopped = false;
        while (!isStopped()) {
            Socket clientSocket;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("Server Stopped.");
                    break;
                }
                throw new RuntimeException("Error accepting core connection", e);
            }
            execute(new TaskRunner(clientSocket));
            System.out.println(threadPool);
        }
        this.threadPool.shutdown();
        System.out.println("Server Stopped.");
    }


    private synchronized boolean isStopped() {
        return this.stopped;
    }

    public synchronized void stop() throws IOException { //todo use read write lock here
        this.stopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
        //close open sockets
        socketManager.close();
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port ", e); //TODO
        }
    }


    public synchronized void setPredecessor(ReferenceNode predecessor) throws IOException {
        if (predecessor.equals(this.predecessor)) {
            return;
        }
        if (this.predecessor.isSelfNode()) {
            socketManager.add(predecessor);
            this.predecessor = predecessor;
            Logger.debug("updated[1] predecessor to: " + predecessor);
            return;
        }

        if (predecessor.isSelfNode()) {
            socketManager.remove(this.predecessor);
            this.predecessor = predecessor;
            Logger.debug("updated[2] predecessor to: " + predecessor);
            return;
        }
        socketManager.swap(this.predecessor, predecessor);
        this.predecessor = predecessor;
        Logger.debug("updated[3] predecessor to: " + predecessor);
    }

    public ReferenceNode getSuccessor() {
        return fingerTable.table[0].successor;
    }

    public synchronized void setSuccessor(ReferenceNode successor) throws IOException {
        ReferenceNode oldSuccessor = fingerTable.table[0].successor;
        if (successor.equals(oldSuccessor)) {
            return;
        }
        if (oldSuccessor.isSelfNode()) {
            socketManager.add(successor);
            fingerTable.table[0].successor = successor;
            Logger.debug("updated[1] successor to: " + successor);
            return;
        }

        if (successor.isSelfNode()) {
            socketManager.remove(fingerTable.table[0].successor);
            fingerTable.table[0].successor = successor;
            Logger.debug("updated[2] successor to: " + successor);
            return;
        }
        socketManager.swap(fingerTable.table[0].successor, successor);
        fingerTable.table[0].successor = successor;
        Logger.debug("updated[3] successor to: " + successor);
    }

    @Override
    public String toString() {
        return "Node{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", id=" + id +
                ", predecessor=" + predecessor +
                ", successor=" + getSuccessor() +
                '}';
    }

    public void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }

    public SocketRunnerManager getSocketManager() {
        return socketManager;
    }
}
