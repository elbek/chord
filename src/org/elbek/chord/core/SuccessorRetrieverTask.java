package org.elbek.chord.core;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by elbek on 9/10/17.
 */
public class SuccessorRetrieverTask extends BaseTask {

    public SuccessorRetrieverTask(Node node) {
        super(node);
    }

    @Override
    public void execute(ByteArray input, DataOutputStream output) {
        try {
            ReferenceNode referenceNode = node.getSuccessor();
            byte[] bytes = referenceNode.toByte();
            output.writeInt(bytes.length);
            output.write(bytes);
        } catch (IOException e) {
            //TODO
        }
    }
}
