package org.elbek.chord.core;

/**
 * Created by elbek on 10/5/17.
 */
public interface RefCountable {
    boolean tryIncrement();
    void decrement();
    int getCount();
}
