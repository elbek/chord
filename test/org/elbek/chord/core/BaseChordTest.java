package org.elbek.chord.core;

import org.elbek.chord.client.Client;
import org.elbek.chord.client.ConnectedState;
import org.elbek.chord.client.commands.JoinCommand;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by elbek on 10/27/17.
 */
public class BaseChordTest {

    /**
     * starts n nodes
     *
     * @param n
     */
    public List<NodeRunner> startNNodes(int n) {
        List<NodeRunner> nodeRunners = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            NodeRunner runner = new NodeRunner();
            int port = ThreadLocalRandom.current().nextInt(10000, 65000);
            runner.startNode("localhost", port, false);
            nodeRunners.add(runner);
        }
        return nodeRunners;
    }

    /**
     * @param joining
     * @param joiner  helps joining node to join to the ring
     */
    public void join(Node joining, Node joiner) {
        Socket socket = null;
        try {
            socket = joining.self.newSocket();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Client client = new Client();
        ConnectedState connectedState = new ConnectedState(client, socket);
        new JoinCommand().execute(new String[]{null, joiner.self.host, String.valueOf(joiner.self.port)}, connectedState);

    }
}
