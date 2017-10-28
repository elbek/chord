package org.elbek.chord.core;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by elbek on 9/10/17.
 */
public class NodeRunner {
    private Node systemNode;
    private ExecutorService nodeService = Executors.newFixedThreadPool(1);
    private ScheduledExecutorService stabilizerService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService fixFingerService = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
        if (args == null) {
            printHelp();
            return;
        }
        int len = args.length;
        String host = null;
        int port = -1;
        boolean debug = false; //TODO, implement debug func
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-d")) {
                debug = true;
            } else if (arg.equals("-h")) {
                if (i + 1 == len) {
                    printHelp();
                    return;
                }
                host = args[i + 1];
                i++;
            } else if (arg.equals("-p")) {
                if (i + 1 == len) {
                    printHelp();
                    return;
                }
                try {
                    port = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("port must be valid number");
                    return;
                }
                i++;
            }
        }
        if (host == null || port == -1) {
            printHelp();
            return;
        }
        NodeRunner nodeRunner = new NodeRunner();
        nodeRunner.startNode(host, port, debug);
    }

    private static void printHelp() {
        System.out.println("accepted params are: -h {host} -p {port} -d");
        System.out.println("-d is optional for enabling debugging, -h and -p is required, host must be valid host name(or ip) and port must be valid port (integer) number");
    }

    void stop() throws IOException {
        systemNode.stop();
        nodeService.shutdown();
        stabilizerService.shutdown();
        fixFingerService.shutdown();
    }

    public Node startNode(String host, int port, boolean debug) {
        systemNode = new Node(host, port, debug);
        nodeService.submit(systemNode);

        stabilizerService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    Stabilizer.stabilize(systemNode);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }, 5, 30, TimeUnit.SECONDS);

        fixFingerService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    Stabilizer.fixFingers(systemNode);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }, 10, 60, TimeUnit.SECONDS);
        System.out.println("Server started, server info: " + systemNode.self);
        return systemNode;
    }

    public Node getNode() {
        return systemNode;
    }
}
