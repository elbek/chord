package org.elbek.chord.client;

/**
 * Created by elbek on 9/21/17.
 */
public class NotConnectedState extends State {


    public NotConnectedState(Client client) {
        super(client);
    }

    @Override
    public String preLine() {
        return "not_connected>";
    }

    @Override
    public void quit() {

    }
}
