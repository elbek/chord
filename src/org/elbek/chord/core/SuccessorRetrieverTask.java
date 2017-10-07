package org.elbek.chord.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by elbek on 9/10/17.
 */
public class SuccessorRetrieverTask extends BaseTask {

    @Override
    public void execute(ByteArray input, OutputStream output) {
        Node node = NodeStarter.runningNode;
        try {
            ReferenceNode referenceNode = node.getSuccessor();
            byte[] bytes = referenceNode.toByte();
            ByteReadWriter.writeInt(output, bytes.length);
            output.write(bytes);
        } catch (IOException e) {
            //TODO
        }
    }
}
