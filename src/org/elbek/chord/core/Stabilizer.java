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
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(referenceNode.host, referenceNode.port));
            SocketClientHelper.sentMessage(TaskRunner.TASKS.SUCCESSOR_FINDER, node.id.toByteArray(), socket, false);
            ByteArray byteArray = ByteArray.from(socket.getInputStream());
            ReferenceNode successor = ReferenceNode.read(byteArray);
            assert successor != null;
            node.setSuccessor(successor);
        } finally {
            socket.close();
        }
    }

    static void fixFingers(Node node) throws IOException, InterruptedException {
        if (node.predecessor.equals(node.self)) {
            return;
        }
        for (int i = 0; i < node.fingerTable.table.length; i++) {
            FingerTable.Entry entry = node.fingerTable.table[i];
            SocketTaskRunner.add(node, node.predecessor, new SocketTaskRunner.SocketTask() {
                @Override
                void doIt(Socket socket) throws IOException {
                    SocketClientHelper.sentMessage(TaskRunner.TASKS.SUCCESSOR_FINDER, entry.start.toByteArray(), socket, true);
                    ByteArray byteArray = ByteArray.from(socket.getInputStream());
                    ReferenceNode newOne = ReferenceNode.read(byteArray);

                    if (!newOne.equals(entry.successor)) {
                        Socket s = new Socket();
                        s.connect(new InetSocketAddress(entry.successor.host, entry.successor.port));
                        node.fingerTable.putSocketRunner(entry.successor, new SocketTaskRunner(s, entry.successor));
                        ReferenceNode old = entry.successor;
                        entry.successor = newOne;
                        if (node.fingerTable.getSocketRunner(old) != null) {
                            node.fingerTable.removeSocketRunner(old);
                            node.fingerTable.getSocketRunner(old).stop();
                        }
                    }
                }
            });
            Thread.sleep(500); //do not hammer the socket runner with fixing, it is not that often updates happen
        }
    }

    static void stabilize(Node node) throws IOException {
        ReferenceNode successor = node.getSuccessor();
        boolean selfSuccessor = successor.equals(node.self);
        if (selfSuccessor) {
            return; //TODO, figure out what to do here
        } else {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            SocketTaskRunner.add(node, node.getSuccessor(), new SocketTaskRunner.SocketTask(countDownLatch) {
                @Override
                void doIt(Socket socket) throws IOException {
                    SocketClientHelper.sentMessage(TaskRunner.TASKS.RETRIEVE_PREDECESSOR, successor.id.toByteArray(), socket, true);
                    ByteArray byteArray = ByteArray.from(socket.getInputStream());
                    ReferenceNode newSuccessor = ReferenceNode.read(byteArray);
                    //check if new successor is between self and successor, if this is not ourselves
                    if (Util.isInRange(node.id, node.getSuccessor().id, newSuccessor.id, false, false)) { //TODO, we should do this atomic
                        node.setSuccessor(newSuccessor);
                    }
                }
            });
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                //TODO, we should figure this out if something goes wrong here, java will interrupt the thread and node will be out of network
            }

            SocketTaskRunner.add(node, node.getSuccessor(), new SocketTaskRunner.SocketTask() {
                @Override
                void doIt(Socket socket) throws IOException {
                    SocketClientHelper.sentMessage(TaskRunner.TASKS.PREDECESSOR_UPDATE, node.self.toByte(), socket, true);
                }
            });
        }
    }
}
