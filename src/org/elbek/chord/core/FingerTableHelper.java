package org.elbek.chord.core;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Created by elbek on 9/10/17.
 */
public class FingerTableHelper {
    public static ReferenceNode findSuccessor(Node node, BigInteger id) throws IOException {
        if (id.equals(node.id)) { //if id is node id, then this node is successor
            return node.self;
        }
        ReferenceNode predecessor = findPredecessor(node, id);
        //check if found predecessor is our predecessor
        if (predecessor == node.predecessor) {
            return node.self;
        }

        //check if found predecessor is node itself, then node's successor is the one that should be returned
        if (predecessor == node.self) {
            return node.getSuccessor();
        }
        SocketTaskRunner socketRunner = node.fingerTable.getSocketRunner(predecessor);
        final ReferenceNode[] successor = new ReferenceNode[1];
        Socket newSocket = null;
        try {
            if (socketRunner == null) {
                newSocket = new Socket(predecessor.host, predecessor.port);
                SocketClientHelper.sentMessage(TaskRunner.TASKS.RETRIEVE_SUCCESSOR, predecessor.id.toByteArray(), newSocket, false);
                ByteArray byteArray = ByteArray.from(newSocket.getInputStream());
                successor[0] = ReferenceNode.read(byteArray);
            } else {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                socketRunner.add(new SocketTaskRunner.SocketTask(countDownLatch) {
                    @Override
                    void doIt(Socket socket) throws IOException {
                        SocketClientHelper.sentMessage(TaskRunner.TASKS.RETRIEVE_SUCCESSOR, predecessor.id.toByteArray(), socket, false);
                        ByteArray byteArray = ByteArray.from(socket.getInputStream());
                        successor[0] = ReferenceNode.read(byteArray);
                    }
                });
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } finally {
            if (newSocket != null) {
                newSocket.close();
            }
        }
        return successor[0];

    }

    public static ReferenceNode findPredecessor(Node node, BigInteger id) throws IOException {

        //if target id is between node and it's predecessor then node's predecessor is the predecessor of the id
        if (Util.isInRange(node.predecessor.id, node.id, id, true, false)) { //this is very important, it is not in the paper, based on what hari says
            return node.predecessor;
        }
        //if id sits between node and it's successor then node itself is the predecessor
        if (Util.isInRange(node.id, node.getSuccessor().id, id, false, true)) { //this is very important, it is not in the paper, based on what hari says
            return node.self;
        }

        final ReferenceNode[] predecessorNode = new ReferenceNode[1];
        FingerTable.Entry entry;
        entry = closestPrecedingFinger(node, id);
        assert entry != null;
        //check if this current node is successor of given id
        if (entry.successor == node.self) {
            return node.predecessor;
        }
        SocketTaskRunner socketRunner = node.fingerTable.getSocketRunner(entry.successor);
        assert socketRunner != null;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        socketRunner.add(new SocketTaskRunner.SocketTask(countDownLatch) {
            @Override
            void doIt(Socket socket) throws IOException {
                SocketClientHelper.sentMessage(TaskRunner.TASKS.PREDECESSOR_FINDER, id.toByteArray(), socket, true);
                ByteArray byteArray = ByteArray.from(socket.getInputStream());
                predecessorNode[0] = ReferenceNode.read(byteArray);
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return predecessorNode[0];

    }

    public static FingerTable.Entry closestPrecedingFinger(Node node, BigInteger id) {
        for (int i = RingHelper.m - 1; i >= 0; i--) {
            FingerTable.Entry entry = node.fingerTable.table[i];
            if (Util.isInRange(entry.start, entry.end, id, true, false)) {
                return entry;
            }
        }
        return null; //TODO, not in the paper this method should return node itself, not it's successor, but if not finger table doesn't have what we are looking for then in order to do a progress we should move to its successor.
    }
}
