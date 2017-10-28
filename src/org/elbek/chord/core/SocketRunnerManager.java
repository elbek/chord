package org.elbek.chord.core;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager class for managing pooled socket runner objects
 * it offers helpful methods for swapping, adding and removing socket mapped SocketRunner objects
 * Created by elbek on 10/9/17.
 */
public class SocketRunnerManager {
    private Map<ReferenceNode, SocketRunner> socketRunnerMap = new ConcurrentHashMap<>();
    Node node;

    public SocketRunnerManager(Node node) {
        this.node = node;
    }

    SocketRunner getSocketRunner(ReferenceNode referenceNode) {
        return socketRunnerMap.get(referenceNode);
    }

    void add(ReferenceNode newNode) throws IOException {
        assert !socketRunnerMap.containsKey(newNode) || node.getPredecessor() == newNode: newNode + " already exists in socket manager";
        SocketRunner newSocketRunner = new SocketRunner(newNode, node);
        node.execute(newSocketRunner);
        socketRunnerMap.put(newNode, newSocketRunner);
    }

    void remove(ReferenceNode referenceNode) throws IOException {
        assert socketRunnerMap.containsKey(referenceNode) : referenceNode + " doesn't exists in socket manager";
        SocketRunner socketRunner = socketRunnerMap.remove(referenceNode);
        socketRunner.stop();
    }

    /**
     * swaps the old one with the new one and closes the old SocketRunner
     *
     * @param oldNode
     * @param newNode
     * @throws IOException
     */
    void swap(ReferenceNode oldNode, ReferenceNode newNode) throws IOException {
        if (!socketRunnerMap.containsKey(newNode)) {
            SocketRunner newSocketRunner = new SocketRunner(newNode, node);
            node.execute(newSocketRunner);
            socketRunnerMap.putIfAbsent(newNode, newSocketRunner);
        }
        SocketRunner oldSocketRunner = socketRunnerMap.remove(oldNode);
        if (oldSocketRunner != null) {
            oldSocketRunner.stop();
        }
    }

    public void close() throws IOException {
        for (ReferenceNode referenceNode : socketRunnerMap.keySet()) {
            if (getSocketRunner(referenceNode) != null) {
                getSocketRunner(referenceNode).getSocket().close();
            }
        }
    }
}
