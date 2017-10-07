package org.elbek.chord.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by elbek on 9/10/17.
 */
public class PredecessorUpdateTask extends BaseTask {

    @Override
    public void execute(ByteArray input, OutputStream output) {
        Node node = NodeStarter.runningNode;
        try {
            ReferenceNode newPredecessor = ReferenceNode.read(input);
            if (Util.isInRange(node.predecessor.id, node.id, newPredecessor.id, false, false)) {
                node.setPredecessor(newPredecessor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
