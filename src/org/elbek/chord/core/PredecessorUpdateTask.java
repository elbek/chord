package org.elbek.chord.core;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by elbek on 9/10/17.
 */
public class PredecessorUpdateTask extends BaseTask {
    public PredecessorUpdateTask(Node node) {
        super(node);
    }

    @Override
    public void execute(ByteArray input, DataOutputStream output) {
        try {
            ReferenceNode newPredecessor = ReferenceNode.read(input);
            if (Util.isInRange(node.getPredecessor().id, node.id, newPredecessor.id, false, false)) {
                node.setPredecessor(newPredecessor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
