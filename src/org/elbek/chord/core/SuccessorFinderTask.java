package org.elbek.chord.core;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Created by elbek on 9/10/17.
 */
public class SuccessorFinderTask extends BaseTask {

    public SuccessorFinderTask(Node node) {
        super(node);
    }

    @Override
    public void execute(ByteArray input, DataOutputStream output) {
        try {
            int len = ByteReadWriter.readInt(input);
            byte b[] = new byte[len];
            input.read(b);
            BigInteger id = new BigInteger(b);
            ReferenceNode referenceNode = FingerTableHelper.findSuccessor(id, node);
            byte[] bytes = referenceNode.toByte();
            output.writeInt(bytes.length);
            System.out.println("sending back:" + bytes.length + " class :" + this.getClass().getSimpleName());
            output.write(bytes);
        } catch (IOException e) {
            //TODO
        }
    }
}
