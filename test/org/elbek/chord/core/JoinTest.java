package org.elbek.chord.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by elbek on 10/28/17.
 */
public class JoinTest extends BaseChordTest {
    List<NodeRunner> runners;

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
        Thread.sleep(100000);
    }

}