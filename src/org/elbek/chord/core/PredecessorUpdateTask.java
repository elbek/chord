package org.elbek.chord.core;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by elbek on 9/10/17.
 */
public class PredecessorUpdateTask extends BaseTask {

    @Override
    public void execute(ByteArray input, DataOutputStream output) {
        Node node = NodeStarter.systemNode;
        try {
            ReferenceNode newPredecessor = ReferenceNode.read(input);
            assert newPredecessor != null : "read newPredecessor null from socket";
            if (Util.isInRange(node.predecessor.id, node.id, newPredecessor.id, false, false)) {
                node.setPredecessor(newPredecessor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
