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
    final BigInteger id;
    private ReferenceNode predecessor;
    final ReferenceNode self;
    final FingerTable fingerTable;
    private SocketRunnerManager socketManager = new SocketRunnerManager(this);
    final private String host;
    final private int port;
    private ServerSocket serverSocket = null;
    private boolean stopped = true;
    private ExecutorService threadPool = Executors.newFixedThreadPool(100);
    boolean debug = false;

    public Node(String host, int port, boolean debug) {
        this.port = port;
        this.host = host;
        this.debug = debug;
        id = new BigInteger(1, HashUtil.SHA1(String.format("%s:%d", host, port))).mod(RingHelper.modulo);
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
            execute(new TaskRunner(clientSocket, this));
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
        //stop open sockets
        socketManager.stop();
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
        if (this.predecessor.isSelfNode(this)) {
            socketManager.add(predecessor);
            this.predecessor = predecessor;
            Logger.debug("updated[1] predecessor to: " + predecessor, this);
            return;
        }

        if (predecessor.isSelfNode(this)) {
            socketManager.remove(this.predecessor);
            this.predecessor = predecessor;
            Logger.debug("updated[2] predecessor to: " + predecessor, this);
            return;
        }
        socketManager.swap(this.predecessor, predecessor);
        this.predecessor = predecessor;
        Logger.debug("updated[3] predecessor to: " + predecessor, this);
    }

    public ReferenceNode getSuccessor() {
        return fingerTable.table[0].successor;
    }

    public synchronized void setSuccessor(ReferenceNode successor) throws IOException {
        ReferenceNode oldSuccessor = fingerTable.table[0].successor;
        if (successor.equals(oldSuccessor)) {
            return;
        }
        if (oldSuccessor.isSelfNode(this)) {
            socketManager.add(successor);
            fingerTable.table[0].successor = successor;
            Logger.debug("updated[1] successor to: " + successor, this);
            return;
        }

        if (successor.isSelfNode(this)) {
            socketManager.remove(fingerTable.table[0].successor);
            fingerTable.table[0].successor = successor;
            Logger.debug("updated[2] successor to: " + successor, this);
            return;
        }
        socketManager.swap(fingerTable.table[0].successor, successor);
        fingerTable.table[0].successor = successor;
        Logger.debug("updated[3] successor to: " + successor, this);
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

    public ReferenceNode getPredecessor() {
        return predecessor;
    }
}
