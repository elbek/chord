package org.elbek.chord.core.commands;

/**
 * Created by elbek on 9/21/17.
 */
public class CommandFactory {

    public static Command getCommand(String code) {
        switch (code) {
            case "h":
                return new HeartBeatCommand();

            case "s":
                return new GetStateCommand();

            case "c":
                return new ConnectCommand();

            case "j":
                return new JoinCommand();

            case "l":
                return new LookUpCommand();
        }
        return null;
    }

    public static String help() {
        return "Valid commands are: h, s, c, j, l";
    }
}
