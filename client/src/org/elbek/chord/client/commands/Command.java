package org.elbek.chord.client.commands;

import org.elbek.chord.client.State;

/**
 * Created by elbek on 9/21/17.
 */
public abstract class Command {

    public abstract void execute(String[] strings, State state);
}
