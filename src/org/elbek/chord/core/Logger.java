package org.elbek.chord.core;

/**
 * Created by elbek on 10/4/17.
 */
public class Logger {
    public static void debug(String message, Node node) {
        if (node.debug) {
            System.out.println(String.format("<%s:%d> %s", node.self.host, node.self.port, message));
        }
    }
}
