package org.elbek.chord.core.commands;

import org.elbek.chord.core.ClientHelper;
import org.elbek.chord.core.ConnectedState;
import org.elbek.chord.core.NotConnectedState;
import org.elbek.chord.core.State;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Created by elbek on 9/21/17.
 */
public class LookUpCommand extends Command {

    public void execute(String[] strings, State state) {
        assert state != null;
        if (state instanceof NotConnectedState) {
            state.println("not connected");
            return;
        }
        if (strings.length < 2) {
            state.println("call join command with l {key} //key is the key you want to find node of it");
            return;
        }
        BigInteger key;
        try {
            key = new BigInteger(strings[1]);
        } catch (NumberFormatException e) {
            state.println("key must be number");
            return;
        }
        ConnectedState connectedState = (ConnectedState) state;
        assert connectedState.getSocket() != null;
        try {
            ClientHelper.sentMessage(ClientHelper.TASKS.SUCCESSOR_FINDER, key.toByteArray(), connectedState.getSocket(), true);
            DataInputStream dis = new DataInputStream(new BufferedInputStream(connectedState.getSocket().getInputStream()));
            int len = dis.readInt();
            byte bytes[] = new byte[len];
            dis.readFully(bytes);
            state.println("host and port is: "+ new String(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
