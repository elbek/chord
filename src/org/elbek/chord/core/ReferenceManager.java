package org.elbek.chord.core;

import java.io.IOException;

/**
 * Created by elbek on 10/5/17.
 */
public class ReferenceManager<T extends RefCountable> {
    protected volatile T current;

    public final T acquire () throws IOException {
        T ref;
        do {
            if ((ref = current) == null) {
                throw new IOException("closed");
            }
            if (ref.tryIncrement()) {
                return ref;
            }
            if (ref.getCount() == 0 && current == ref) {
                throw new IOException("closed");
            }
        } while (true);
    }

    public void release(T ref) {
        ref.decrement();
    }

    public synchronized void swap (T newOne) {
        final T old = current;
        current = newOne;
        release(old);
    }

    public synchronized void close () {
        if (current != null) {
            swap(null);
        }
    }
}
