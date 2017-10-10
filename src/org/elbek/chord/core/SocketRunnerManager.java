package org.elbek.chord.core;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by elbek on 10/9/17.
 */
public class SocketRunnerManager {
    private Map<ReferenceNode, SocketRunner> socketRunnerMap = new ConcurrentHashMap<>();

    SocketRunner getSocketRunner(ReferenceNode referenceNode) {
        return socketRunnerMap.get(referenceNode);
    }

    void add(ReferenceNode newNode) throws IOException {
        assert !socketRunnerMap.containsKey(newNode) : newNode + " already exists in socket manager";
        SocketRunner newSocketRunner = new SocketRunner(newNode);
        NodeStarter.systemNode.execute(newSocketRunner);
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
            SocketRunner newSocketRunner = new SocketRunner(newNode);
            NodeStarter.systemNode.execute(newSocketRunner);
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
