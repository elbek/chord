package org.elbek.chord.core;

import java.io.Console;
import java.util.Scanner;
import java.util.Stack;

/**
 * Created by elbek on 9/21/17.
 */
public class Client {
    Stack<State> stack = new Stack<>();
    Console console;

    public Stack<State> getStack() {
        return stack;
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.console = System.console();
        if (client.console == null) {
            System.out.println("console is not available");
        }
        Stack<State> stack = client.stack;
        stack.add(new NotConnectedState(client));
        Scanner scanner = new Scanner(System.in);
        while (!stack.isEmpty()) {
            System.out.print(stack.peek().preLine());
            String line = scanner.nextLine();
            if (line == null || line.isEmpty()) {
                continue;
            }
            if ("q".equals(line.trim())) {
                stack.pop().quit();
                continue;
            }
            String[] strings = line.split("\\s+");
            if (strings.length == 0) {
                continue;
            }
            stack.peek().perform(strings);
        }
        System.out.println("you are disconnected");
    }
}
