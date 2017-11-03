package org.elbek.chord.core;

import org.elbek.chord.client.ClientHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by elbek on 10/28/17.
 */
public class JoinTest extends BaseChordTest {
    private List<NodeRunner> runners;

    @Before
    public void setUp() throws Exception {
        runners = startNNodes(5);
    }

    @After
    public void tearDown() throws Exception {
        for (NodeRunner runner : runners) {
            runner.stop();
        }
    }

    @Test
    public void joinTest() throws Exception {
        for (int i = 1; i < runners.size(); i++) {
            int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    join(runners.get(finalI).getNode(), runners.get(0).getNode());
                }
            }).start();
        }
        //give 15 seconds for 5 servers to link up ech other
        Thread.sleep(50000);
        runners.sort(new Comparator<NodeRunner>() {
            @Override
            public int compare(NodeRunner o1, NodeRunner o2) {
                return o1.getNode().self.id.compareTo(o2.getNode().self.id);
            }
        });
        for (NodeRunner runner : runners) {
            System.out.println(runner);
        }
        for (int i = 0; i < runners.size(); i++) {
            Node node = runners.get(i).getNode();
            ReferenceNode predecessor = node.getPredecessor();
            ReferenceNode successor = node.getSuccessor();
            Node prevNode = (i == 0 ? runners.get(runners.size() - 1) : runners.get(i - 1)).getNode();
            Node nextNode = (i == runners.size() - 1 ? runners.get(0) : runners.get(i + 1)).getNode();
            Assert.assertEquals(prevNode.self, predecessor);
            Assert.assertEquals(nextNode.self, successor);
        }
        Thread.sleep(10000000);

        for (int i = 0; i < runners.size(); i++) {
            Node node = runners.get(i).getNode();
            ReferenceNode predecessor = node.getPredecessor();
            ReferenceNode successor = node.getSuccessor();
            for (NodeRunner runner : runners) {
                Assert.assertEquals(successor, FingerTableHelper.findSuccessor(node.id.add(BigInteger.ONE), runner.getNode()));
            }
        }
    }
}