package org.elbek.chord.core;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by elbek on 9/21/17.
 */
public class ConnectedState extends State {
    private Socket socket;

    public ConnectedState(Client client) {
        super(client);
    }

    @Override
    public String preLine() {
        return String.format("%s:%d>", socket.getInetAddress().getHostName(), socket.getPort());
    }

    @Override
    public void quit() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
