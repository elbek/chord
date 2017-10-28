package org.elbek.chord.client;

import java.io.Console;
import java.util.Scanner;
import java.util.Stack;

/**
 * Created by elbek on 9/21/17.
 */
public class Client {
    private Stack<State> stack = new Stack<>();
    Console console;

    public Client() {
        console = System.console();
        if (console == null) {
            System.out.println("console is not available");
        }
    }

    public Stack<State> getStack() {
        return stack;
    }

    public static void main(String[] args) {
        Client client = new Client();
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
