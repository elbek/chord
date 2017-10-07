package org.elbek.chord.core;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by elbek on 9/10/17.
 */
public class Node implements Runnable {
    BigInteger id;
    volatile ReferenceNode predecessor;
    ReferenceNode self;
    FingerTable fingerTable;
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
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("Server Stopped.");
                    break;
                }
                throw new RuntimeException("Error accepting core connection", e);
            }
            this.threadPool.execute(new TaskRunner(clientSocket));
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
        fingerTable.close();
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port ", e); //TODO
        }
    }


    public synchronized void setPredecessor(ReferenceNode predecessor) throws IOException {
        if (!predecessor.equals(this.predecessor)) {
            ReferenceNode old = this.predecessor;
            this.predecessor = predecessor;
            if (!predecessor.equals(self)) {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(this.predecessor.host, this.predecessor.port));
                SocketTaskRunner socketTaskRunner = new SocketTaskRunner(socket, predecessor);
                submit(socketTaskRunner);
                fingerTable.putSocketRunner(this.predecessor, socketTaskRunner);
            }
            SocketTaskRunner socketRunner = fingerTable.getSocketRunner(old);
            if (socketRunner != null) {
                socketRunner.stop();
            }
            Logger.debug("updated predecessor to: " + predecessor);
        }
    }

    public ReferenceNode getSuccessor() {
        return fingerTable.table[0].successor;
    }

    public synchronized void setSuccessor(ReferenceNode successor) throws IOException {
        ReferenceNode old = fingerTable.table[0].successor;
        if (!successor.equals(self)) { //if this is not us
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(successor.host, successor.port));
            SocketTaskRunner socketTaskRunner = new SocketTaskRunner(socket, successor);
            submit(socketTaskRunner);
            fingerTable.putSocketRunner(successor, socketTaskRunner);
        }
        fingerTable.table[0].successor = successor;
        if (!old.equals(self)) {
            SocketTaskRunner socketRunner = fingerTable.getSocketRunner(old);
            if (socketRunner != null) {
                socketRunner.stop();
                fingerTable.removeSocketRunner(old);
            }
        }
        Logger.debug("updated successor to: " + successor);
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

    public void submit(Runnable runnable) {
        threadPool.submit(runnable);
    }
}
