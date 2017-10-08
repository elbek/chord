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
        ReferenceNode predecessor = findPredecessor(node, id); //TODO, this may return null, handle it properly
        //check if found predecessor is our predecessor
        if (predecessor == node.predecessor) {
            return node.self;
        }

        //check if found predecessor is node itself, then node's successor is the one that should be returned
        if (predecessor == node.self) {
            return node.getSuccessor();
        }
        ReferenceNode successor;
        try (Socket socket = predecessor.newSocket()) {
            SocketClientHelper.sentMessage(TaskRunner.TASKS.RETRIEVE_SUCCESSOR, predecessor.id.toByteArray(), socket, false);
            ByteArray byteArray = ByteArray.from(socket.getInputStream());
            successor = ReferenceNode.read(byteArray);
        }
        return successor;

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

        FingerTable.Entry entry;
        entry = closestPrecedingFinger(node, id);
        assert entry != null;
        //check if this current node is successor of given id
        if (entry.successor == node.self) {
            return node.predecessor;
        }
        try(Socket socket = entry.successor.newSocket()) {
            SocketClientHelper.sentMessage(TaskRunner.TASKS.PREDECESSOR_FINDER, id.toByteArray(), socket, false);
            ByteArray byteArray = ByteArray.from(socket.getInputStream());
            return ReferenceNode.read(byteArray);

        }
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
