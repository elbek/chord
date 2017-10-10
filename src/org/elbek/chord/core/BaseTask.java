package org.elbek.chord.core;

import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 * Created by elbek on 9/10/17.
 */
public abstract class BaseTask {

    public abstract void execute(ByteArray input, DataOutputStream output);
}
