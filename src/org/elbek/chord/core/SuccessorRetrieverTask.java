package org.elbek.chord.core;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by elbek on 9/10/17.
 */
public class SuccessorRetrieverTask extends BaseTask {

    @Override
    public void execute(ByteArray input, DataOutputStream output) {
        Node node = NodeStarter.systemNode;
        try {
            ReferenceNode referenceNode = node.getSuccessor();
            byte[] bytes = referenceNode.toByte();
            output.writeInt(bytes.length);
            System.out.println("sending back:" + bytes.length + " class :" + this.getClass().getSimpleName() + " data "+ Arrays.toString(bytes));
            output.write(bytes);
        } catch (IOException e) {
            //TODO
        }
    }
}
