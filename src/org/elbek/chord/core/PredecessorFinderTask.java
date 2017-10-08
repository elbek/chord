package org.elbek.chord.core;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Created by elbek on 9/10/17.
 */
public class PredecessorFinderTask extends BaseTask { //TODO

    @Override
    public void execute(ByteArray input, OutputStream output) {
        Node node = NodeStarter.runningNode;
        try {
            int len = ByteReadWriter.readInt(input);
            byte b[] = new byte[len];
            input.read(b);
            BigInteger id = new BigInteger(b); //TODO, check if this is right
            ReferenceNode referenceNode = FingerTableHelper.findPredecessor(node, id);
            byte[] bytes = referenceNode.toByte();
            ByteReadWriter.writeInt(output, bytes.length);
            System.out.println("sending back:" + bytes.length + " class :" + this.getClass().getSimpleName() + " data "+ Arrays.toString(bytes));
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
