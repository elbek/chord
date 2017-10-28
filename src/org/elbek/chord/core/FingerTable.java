package org.elbek.chord.core;

import java.math.BigInteger;

/**
 * Created by elbek on 9/10/17.
 */
public class FingerTable {
    Node node;
    Entry[] table = new Entry[RingHelper.m];
    public FingerTable(Node node) {
        this.node = node;
    }

    static FingerTable build(Node node, ReferenceNode successor) {
        FingerTable fingerTable = new FingerTable(node);
        for (int i = 0; i < RingHelper.m; i++) {
            if (i == RingHelper.m - 1) {
                fingerTable.table[i] = new Entry(node.id.add(RingHelper.twoPowers[i - 1]).mod(RingHelper.modulo), node.id, successor, i);
            } else {
                BigInteger start;
                if (i == 0) {
                    start = node.id.add(RingHelper.twoPowers[i]).mod(RingHelper.modulo);
                } else {
                    start = fingerTable.table[i - 1].end;
                }
                BigInteger end = node.id.add(RingHelper.twoPowers[i + 1]).mod(RingHelper.modulo);
                fingerTable.table[i] = new Entry(start, end, successor, i);
            }
        }
        return fingerTable;
    }

    static class Entry {
        BigInteger start;
        BigInteger end;
        ReferenceNode successor;
        int index;

        public Entry(BigInteger start, BigInteger end, ReferenceNode successor, int index) {
            this.start = start;
            this.end = end;
            this.successor = successor;
            this.index = index;
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
