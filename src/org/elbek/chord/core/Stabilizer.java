package org.elbek.chord.core;

import java.io.IOException;
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
        try (Socket socket = referenceNode.newSocket()) {//this new socket is short living living object, so open the socket and close it afterwards
            SocketClientHelper.sentMessage(TaskRunner.TASKS.SUCCESSOR_FINDER, node.id.toByteArray(), node, socket, false);
            ByteArray byteArray = ByteArray.from(socket.getInputStream());
            ReferenceNode successor = ReferenceNode.read(byteArray);
            node.setSuccessor(successor);
        }
    }

    static void fixFingers(Node node) throws IOException, InterruptedException {
        if (node.getPredecessor().equals(node.self)) {
            return; //looks like node hasn't totally joined to the network yet, wait until full join happens (ie succ and pre are updated with neighbour nodes in the ring)
        }
        for (int i = 1; i < node.fingerTable.table.length; i++) { //do not update successor here. ie start from 1
            FingerTable.Entry entry = node.fingerTable.table[i];
            ReferenceNode newOne = FingerTableHelper.findSuccessor(entry.start, node);
            if (!newOne.equals(entry.successor)) {
                node.getSocketManager().swap(entry.successor, newOne);
                entry.successor = newOne;
            }
            Thread.sleep(500); //do not hammer the socket runner with fixing, it is not that often updates happen
        }
    }

    /**
     * Stabilizes the system node bu running ...
     * this node has to use CountDownLatch to block the current thread, because we don't want to fill the socket runner queue by leaving before task finishes,
     * because after x amount seconds we run this again periodically
     *
     * @param node
     * @throws IOException
     */
    static void stabilize(Node node) throws IOException {
        CountDownLatch latch = new CountDownLatch(1);
        SocketRunner.add(new SocketRunner.Resolver() {
            @Override
            public ReferenceNode resolve() {
                return node.getSuccessor();
            }
        }, new SocketRunner.SocketTask(latch) {
            @Override
            void doIt(Socket socket, ReferenceNode resolvedNode) throws IOException {

                if (socket == null) {
                    assert resolvedNode.isSelfNode(node) : "socket passed null when " + resolvedNode + " is not self"; //this is true because only this method updates successor periodically, no concurrent updates!
                    ReferenceNode newSuccessor = node.getPredecessor(); //since successor is node itself, our predecessor can be our successor.
                    //check if new successor is between self and successor, this happens if someone updated predecessor but successor is still node itself
                    if (Util.isInRange(node.id, resolvedNode.id, newSuccessor.id, false, false)) {
                        node.setSuccessor(newSuccessor);
                    }
                } else {
                    SocketClientHelper.sentMessage(TaskRunner.TASKS.RETRIEVE_PREDECESSOR, resolvedNode.id.toByteArray(), node, socket, true);
                    ByteArray byteArray = ByteArray.from(socket.getInputStream());
                    ReferenceNode newSuccessor = ReferenceNode.read(byteArray);
                    //check if new successor is between self and successor, if this is not ourselves
                    if (Util.isInRange(node.id, resolvedNode.id, newSuccessor.id, false, false)) {
                        node.setSuccessor(newSuccessor);
                    }
                }
            }
        }, node);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            //we are just one call away, keep going.
        }

        latch = new CountDownLatch(1);
        SocketRunner.add(new SocketRunner.Resolver() {
            @Override
            public ReferenceNode resolve() {
                return node.getSuccessor();
            }
        }, new SocketRunner.SocketTask(latch) {
            @Override
            void doIt(Socket socket, ReferenceNode resolvedNode) throws IOException {
                if (socket == null) {
                    //TODO, we should figure out what to do when socket is self node
                } else {
                    SocketClientHelper.sentMessage(TaskRunner.TASKS.PREDECESSOR_UPDATE, node.self.toByte(), node, socket, true);
                }
            }
        }, node);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}