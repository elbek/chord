package org.elbek.chord.core;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Created by elbek on 9/10/17.
 */
public class FingerTableHelper {
    public static ReferenceNode findSuccessor(BigInteger id) throws IOException {
        Node node = NodeStarter.systemNode;
        ReferenceNode predecessor = findPredecessor(id);
        //check if found predecessor is sys predecessor
        if (predecessor == node.predecessor) {
            return node.self;
        }

        //check if found predecessor is sys node itself, then sys successor is the one that should be returned
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

    public static ReferenceNode findPredecessor(BigInteger id) throws IOException {
        Node node = NodeStarter.systemNode;
        //if target id is between node and it's predecessor then node's predecessor is the predecessor of the id
        if (Util.isInRange(node.predecessor.id, node.id, id, false, true)) {
            return node.predecessor;
        }
        //if id sits between node and it's successor then node itself is the predecessor
        if (Util.isInRange(node.id, node.getSuccessor().id, id, false, true)) {
            return node.self;
        }

        FingerTable.Entry entry;
        entry = closestPrecedingFinger(id);
        assert entry != null;
        //check if this current node is successor of given id
        if (entry.successor == node.self) {
            return node.predecessor;
        }
        final int index = entry.index;
        assert index > 0 && index < RingHelper.m : "index=" + index + " is invalid";
        ReferenceNode[] result = new ReferenceNode[1];
        CountDownLatch latch = new CountDownLatch(1);
        SocketRunner.add(new SocketRunner.Resolver() {
            @Override
            public ReferenceNode resolve() {
                return node.fingerTable.table[index].successor;
            }
        }, new SocketRunner.SocketTask(latch) {
            @Override
            void doIt(Socket socket, ReferenceNode referenceNode) throws IOException {
                if (socket == null) {
                    result[0] = node.predecessor; //TODO, investigate more and see when this might happen
                } else {
                    SocketClientHelper.sentMessage(TaskRunner.TASKS.PREDECESSOR_FINDER, id.toByteArray(), socket, true);
                    ByteArray byteArray = ByteArray.from(socket.getInputStream());
                    result[0] = ReferenceNode.read(byteArray);
                }
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            //TODO, maybe we wait even if interrupted, socket will time out soon and exception is thrown if method won't finish fast
        }
        return result[0];

    }

    public static FingerTable.Entry closestPrecedingFinger(BigInteger id) {
        Node node = NodeStarter.systemNode;
        for (int i = RingHelper.m - 1; i >= 0; i--) {
            FingerTable.Entry entry = node.fingerTable.table[i];
            if (Util.isInRange(entry.start, entry.end, id, true, false)) {
                return entry;
            }
        }
        //if we hit here, then we have a bug, the for statement above should cover entire ring
        throw new RuntimeException("this is a bug");
    }
}
