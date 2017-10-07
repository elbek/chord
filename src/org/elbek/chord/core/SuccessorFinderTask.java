package org.elbek.chord.core;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * Created by elbek on 9/10/17.
 */
public class SuccessorFinderTask extends BaseTask {

    @Override
    public void execute(ByteArray input, OutputStream output) {
        Node node = NodeStarter.runningNode;
        try {
            int len = ByteReadWriter.readInt(input);
            byte b[] = new byte[len];
            input.read(b);
            BigInteger id = new BigInteger(b);
            ReferenceNode referenceNode = FingerTableHelper.findSuccessor(node, id);
            byte[] bytes = referenceNode.toByte();
            ByteReadWriter.writeInt(output, bytes.length);
            output.write(bytes);
        } catch (IOException e) {
            //TODO
        }
    }
}
