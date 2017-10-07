package org.elbek.chord.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by elbek on 9/10/17.
 */
public class JoinTask extends BaseTask { //TODO
    @Override
    public void execute(ByteArray input, OutputStream output) {
        Node node = NodeStarter.runningNode;
        try {
            int len = ByteReadWriter.readInt(input);
            byte[] b = new byte[len];
            input.read(b);
            ReferenceNode joinerNode = ReferenceNode.fromByte(b);
            Logger.debug("joining thru "+joinerNode);
            Stabilizer.join(node, joinerNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}