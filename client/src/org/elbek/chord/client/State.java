package org.elbek.chord.client;

import org.elbek.chord.client.commands.Command;
import org.elbek.chord.client.commands.CommandFactory;

/**
 * Created by elbek on 9/21/17.
 */
public abstract class State {
    private Client client;

    public State(Client client) {
        this.client = client;
    }

    public abstract String preLine();

    public void perform(String[] strings) {
        Command command = CommandFactory.getCommand(strings[0]);
        if (command == null) {
            println(CommandFactory.help());
            return;
        }
        command.execute(strings, this);
    }

    public void println(String str) {
        if (client.console == null) {
            System.out.print(preLine());
            System.out.println(str);
            System.out.flush();
        } else {
            client.console.writer().print(preLine());
            client.console.writer().println(str);
            client.console.writer().flush();
        }
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public abstract void quit();
}
