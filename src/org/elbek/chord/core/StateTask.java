package org.elbek.chord.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by elbek on 9/10/17.
 */
public class StateTask extends BaseTask {
    @Override
    public void execute(ByteArray input, OutputStream output) {
        Node node = NodeStarter.runningNode;
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = new char[50];
        stringBuilder.append(System.lineSeparator());

        int i = 1;
        stringBuilder.append(i++);
        stringBuilder.append(")");

        Util.prependString(chars, "self");
        stringBuilder.append(chars);

        Util.prependString(chars, node.id.toString());
        stringBuilder.append(chars);

        Util.prependString(chars, new String(node.self.toByte()));
        stringBuilder.append(chars);
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append(i++);
        stringBuilder.append(")");
        Util.prependString(chars, "predecessor");
        stringBuilder.append(chars);

        Util.prependString(chars, node.predecessor.id.toString());
        stringBuilder.append(chars);

        Util.prependString(chars, new String(node.predecessor.toByte()));
        stringBuilder.append(chars);
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());

        for (FingerTable.Entry entry : node.fingerTable.table) {
            stringBuilder.append(i++);
            stringBuilder.append(") ");
            Util.prependString(chars, entry.start.toString());
            stringBuilder.append(chars);

            Util.prependString(chars, entry.end.toString());
            stringBuilder.append(chars);

            if (entry.successor == node.self) {
                Util.prependString(chars, "self");
                stringBuilder.append(chars);
            } else {
                Util.prependString(chars, new String(entry.successor.toByte()));
                stringBuilder.append(chars);
            }
            stringBuilder.append(System.lineSeparator());
        }
        byte[] bytes = stringBuilder.toString().getBytes();
        try {
            ByteReadWriter.writeInt(output, bytes.length);
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}