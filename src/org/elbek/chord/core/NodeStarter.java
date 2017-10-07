package org.elbek.chord.core;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by elbek on 9/10/17.
 */
public class NodeStarter {
    static Node runningNode;
    static private ExecutorService nodeService = Executors.newFixedThreadPool(1);
    static private ScheduledExecutorService stabilizerService = Executors.newSingleThreadScheduledExecutor();
    static private ScheduledExecutorService fixFingerService = Executors.newSingleThreadScheduledExecutor();
    String host = null;
    int port = -1;
    boolean debug = false;

    public static void main(String[] args) {
        if (args == null) {
            printHelp();
            return;
        }
        int len = args.length;
        NodeStarter starter = new NodeStarter();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-d")) {
                starter.debug = true;
            } else if (arg.equals("-h")) {
                if (i + 1 == len) {
                    printHelp();
                    return;
                }
                starter.host = args[i + 1];
                i++;
            } else if (arg.equals("-p")) {
                if (i + 1 == len) {
                    printHelp();
                    return;
                }
                int port;
                try {
                    port = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("port must be valid number");
                    return;
                }
                starter.port = port;
                i++;
            }
        }
        if (starter.host == null || starter.port == -1) {
            printHelp();
            return;
        }
        runningNode = new Node(starter);
        nodeService.submit(runningNode);

        stabilizerService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    Stabilizer.stabilize(runningNode);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }, 2, 5, TimeUnit.SECONDS);

        fixFingerService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    Stabilizer.fixFingers(runningNode);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }, 10000, 30, TimeUnit.SECONDS);
    }

    private static void printHelp() {
        System.out.println("accepted params are: -h {host} -p {port} -d");
        System.out.println("-d is optional for enabling debugging, -h and -p is required, host must be valid host name(or ip) and port must be valid port (integer) number");
    }

    void stop() throws IOException {
        runningNode.stop();
        nodeService.shutdown();
        stabilizerService.shutdown();
        fixFingerService.shutdown();
    }
}
