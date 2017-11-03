package org.elbek.chord.core;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Created by elbek on 9/10/17.
 */
public class PredecessorFinderTask extends BaseTask { //TODO


    public PredecessorFinderTask(Node node) {
        super(node);
    }

    @Override
    public void execute(ByteArray input, DataOutputStream output) {
        try {
            int len = ByteReadWriter.readInt(input);
            byte b[] = new byte[len];
            input.read(b);
            BigInteger id = new BigInteger(b); //TODO, check if this is right
            ReferenceNode referenceNode = FingerTableHelper.findPredecessor(id, node, node.getPredecessor());
            byte[] bytes = referenceNode.toByte();
            output.writeInt(bytes.length);
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
