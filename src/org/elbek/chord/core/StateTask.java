package org.elbek.chord.core;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by elbek on 9/10/17.
 */
public class StateTask extends BaseTask {

    public StateTask(Node node) {
        super(node);
    }

    @Override
    public void execute(ByteArray input, DataOutputStream output) {
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

        ReferenceNode predecessor = node.getPredecessor();

        Util.prependString(chars, predecessor.id.toString());
        stringBuilder.append(chars);

        Util.prependString(chars, new String(predecessor.toByte()));
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
            output.writeInt(bytes.length);
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}