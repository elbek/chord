package org.elbek.chord.core;

/**
 * Created by elbek on 10/4/17.
 */
public class Logger {
    public static void debug(String message) {
        System.out.println(String.format("<%s:%d> %s", NodeStarter.systemNode.self.host, NodeStarter.systemNode.self.port, message));
    }
}
