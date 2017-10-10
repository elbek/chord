package org.elbek.chord.core;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by elbek on 10/10/17.
 */
public class TestClass {
    public static void main(String[] args) {
        ReferenceNode referenceNode78 = new ReferenceNode("localhost", 4578);
        ReferenceNode referenceNode79 = new ReferenceNode("localhost", 4579);
        ReferenceNode referenceNode80 = new ReferenceNode("localhost", 4580);
        class Entry {
            BigInteger id;
            ReferenceNode referenceNode;

            public Entry(BigInteger id, ReferenceNode referenceNode) {
                this.id = id;
                this.referenceNode = referenceNode;
            }

            @Override
            public String toString() {
                return "Entry{" + "id=" + id + ", referenceNode=" + referenceNode + '}';
            }
        }

        List<Entry> list = new ArrayList<>(20);
        list.add(new Entry(referenceNode78.id, referenceNode78));
        list.add(new Entry(referenceNode79.id, referenceNode79));
        list.add(new Entry(referenceNode80.id, referenceNode80));

        for (int i = 0; i < 20; i++) {
            list.add(new Entry(nextRandomBigInteger(list.get(list.size() - 1).id), null));
        }

        Collections.sort(list, new Comparator<Entry>() {
            @Override
            public int compare(Entry o1, Entry o2) {
                return o1.id.compareTo(o2.id);
            }
        });
        for (Entry entry : list) {
            System.out.println(entry);
        }
    }

    public static BigInteger nextRandomBigInteger(BigInteger n) {
        Random rand = new Random();
        BigInteger result = new BigInteger(n.bitLength(), rand);
        while (result.compareTo(n) >= 0) {
            result = new BigInteger(n.bitLength(), rand);
        }
        return result.mod(RingHelper.modulo);
    }
}
