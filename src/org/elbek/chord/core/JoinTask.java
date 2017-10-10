package org.elbek.chord.core;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by elbek on 9/10/17.
 */
public class JoinTask extends BaseTask { //TODO
    @Override
    public void execute(ByteArray input, DataOutputStream output) {
        Node node = NodeStarter.systemNode;
        try {
            int len = ByteReadWriter.readInt(input);
            byte[] b = new byte[len];
            input.read(b);
            ReferenceNode joinerNode = ReferenceNode.fromByte(b);
            Logger.debug("joining thru " + joinerNode);
            Stabilizer.join(node, joinerNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}