package org.elbek.chord.core;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by elbek on 9/10/17.
 */
public class FingerTable {
    Node node;
    Entry[] table = new Entry[RingHelper.m];
    private Map<ReferenceNode, SocketTaskRunner> socketTaskRunnerMap = new ConcurrentHashMap<>();

    public FingerTable(Node node) {
        this.node = node;
    }

    static FingerTable build(Node node, ReferenceNode successor) {
        FingerTable fingerTable = new FingerTable(node);
        for (int i = 0; i < RingHelper.m; i++) {
            if (i == RingHelper.m - 1) {
                fingerTable.table[i] = new Entry(node.id.add(RingHelper.twoPowers[i - 1]).mod(RingHelper.modulo), node.id, successor);
            } else {
                BigInteger start;
                if (i == 0) {
                    start = node.id.add(RingHelper.twoPowers[i]).mod(RingHelper.modulo);
                } else {
                    start = fingerTable.table[i - 1].end;
                }
                BigInteger end = node.id.add(RingHelper.twoPowers[i + 1]).mod(RingHelper.modulo);
                fingerTable.table[i] = new Entry(start, end, successor);
            }
        }
        return fingerTable;
    }

    SocketTaskRunner getSocketRunner(ReferenceNode referenceNode) {
        return node.fingerTable.socketTaskRunnerMap.get(referenceNode);
    }

    void putSocketRunner(ReferenceNode referenceNode, SocketTaskRunner socketTaskRunner) {
        node.fingerTable.socketTaskRunnerMap.put(referenceNode, socketTaskRunner);
    }

    void removeSocketRunner(ReferenceNode referenceNode) {
        Logger.debug("removing SocketTaskRunner for " + referenceNode);
        node.fingerTable.socketTaskRunnerMap.remove(referenceNode);
    }

    public void close() throws IOException {
        for (ReferenceNode referenceNode : socketTaskRunnerMap.keySet()) {
            if (getSocketRunner(referenceNode) != null) {
                getSocketRunner(referenceNode).getSocket().close();
            }
        }
    }

    static class Entry {
        BigInteger start;
        BigInteger end;
        ReferenceNode successor;

        public Entry(BigInteger start, BigInteger end, ReferenceNode successor) {
            this.start = start;
            this.end = end;
            this.successor = successor;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (!start.equals(entry.start)) return false;
            return end.equals(entry.end);
        }

        @Override
        public int hashCode() {
            int result = start.hashCode();
            result = 31 * result + end.hashCode();
            return result;
        }

    }
}
