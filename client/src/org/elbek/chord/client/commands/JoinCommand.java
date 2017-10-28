package org.elbek.chord.client.commands;

import org.elbek.chord.client.ClientHelper;
import org.elbek.chord.client.ConnectedState;
import org.elbek.chord.client.NotConnectedState;
import org.elbek.chord.client.State;

import java.io.IOException;

/**
 * Created by elbek on 9/21/17.
 */
public class JoinCommand extends Command {

    public void execute(String[] strings, State state) {
        assert state != null;
        if (state instanceof NotConnectedState) {
            state.println("not connected");
            return;
        }
        if (strings.length < 3) {
            state.println("call join command with j {host} {port} //host and port here the node that initiates the join");
            return;
        }
        String host = strings[1];
        String port = strings[2];
        try {
            int intPort = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            state.println("port must be number");
            return;
        }
        ConnectedState connectedState = (ConnectedState) state;
        assert connectedState.getSocket() != null;
        try {
            ClientHelper.sentMessage(ClientHelper.TASKS.JOIN, String.format("%s:%s", host, port).getBytes(), connectedState.getSocket(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
