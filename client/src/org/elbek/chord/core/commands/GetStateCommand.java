package org.elbek.chord.core.commands;

import org.elbek.chord.core.ClientHelper;
import org.elbek.chord.core.ConnectedState;
import org.elbek.chord.core.NotConnectedState;
import org.elbek.chord.core.State;

import java.io.IOException;

/**
 * Created by elbek on 9/21/17.
 */
public class GetStateCommand extends Command {

    public void execute(String[] strings, State state) {
        assert state != null;
        if (state instanceof NotConnectedState) {
            state.println("not connected");
            return;
        }
        ConnectedState connectedState = (ConnectedState) state;
        assert connectedState.getSocket() != null;
        byte[] bytes = null;
        try {
            ClientHelper.sentMessage(ClientHelper.TASKS.STATE, null, connectedState.getSocket(), true);
            bytes = ClientHelper.read(connectedState.getSocket());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bytes != null) {
            state.println(new String(bytes));
        }
    }
}