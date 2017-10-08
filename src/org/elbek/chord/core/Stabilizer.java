package org.elbek.chord.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Created by elbek on 9/11/17.
 */
public class Stabilizer {
    /**
     * @param node          the node that is joining to the ring
     * @param referenceNode the node that helps to join the node to the network
     */
    public static void join(Node node, ReferenceNode referenceNode) throws IOException {
        try (Socket socket = referenceNode.newSocket()) {
            SocketClientHelper.sentMessage(TaskRunner.TASKS.SUCCESSOR_FINDER, node.id.toByteArray(), socket, false);
            ByteArray byteArray = ByteArray.from(socket.getInputStream());
            ReferenceNode successor = ReferenceNode.read(byteArray);
            node.setSuccessor(successor);
        }
    }

    static void fixFingers(Node node) throws IOException, InterruptedException {
        if (node.predecessor.equals(node.self)) {
            return;
        }
        for (int i = 0; i < node.fingerTable.table.length; i++) {
            FingerTable.Entry entry = node.fingerTable.table[i];
            try(Socket socket = node.predecessor.newSocket()) {
                SocketClientHelper.sentMessage(TaskRunner.TASKS.SUCCESSOR_FINDER, entry.start.toByteArray(), socket, false);
                ByteArray byteArray = ByteArray.from(socket.getInputStream());
                ReferenceNode newOne = ReferenceNode.read(byteArray);
                if (!newOne.equals(entry.successor)) {
                    entry.successor = newOne;
                }
            }
            Thread.sleep(500); //do not hammer the socket runner with fixing, it is not that often updates happen
        }
    }

    static void stabilize(Node node) throws IOException {
        ReferenceNode successor = node.getSuccessor();
        boolean selfSuccessor = successor.equals(node.self);
        if (selfSuccessor) {
            return; //TODO, figure out what to do here
        } else {
            try (Socket socket = successor.newSocket()) {
                SocketClientHelper.sentMessage(TaskRunner.TASKS.RETRIEVE_PREDECESSOR, successor.id.toByteArray(), socket, false);
                ByteArray byteArray = ByteArray.from(socket.getInputStream());
                ReferenceNode newSuccessor = ReferenceNode.read(byteArray);
                //check if new successor is between self and successor, if this is not ourselves
                if (Util.isInRange(node.id, node.getSuccessor().id, newSuccessor.id, false, false)) { //TODO, we should do this atomic
                    node.setSuccessor(newSuccessor);
                }
            }
            try (Socket socket = successor.newSocket()) {
                //SocketClientHelper.sentMessage(TaskRunner.TASKS.PREDECESSOR_UPDATE, node.self.toByte(), socket, false);
            }
        }
    }
}
