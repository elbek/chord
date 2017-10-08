package org.elbek.chord.core.commands;

import org.elbek.chord.core.ConnectedState;
import org.elbek.chord.core.State;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by elbek on 9/21/17.
 */
public class ConnectCommand extends Command {

    public void execute(String[] strings, State state) {
        if (strings.length<3) {
            state.println("call connect command with c {host} {port}");
            return;
        }
        String port = strings[2];
        String host = strings[1];
        try {
            int intPort = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            state.println("port must be number");
            return;
        }
        Socket socket = null;
        try {
            socket = new Socket(host, Integer.parseInt(port));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        ConnectedState connectedState = new ConnectedState(state.getClient());
        connectedState.setSocket(socket);
        connectedState.getClient().getStack().push(connectedState);
        connectedState.println("you are connected");
    }
}