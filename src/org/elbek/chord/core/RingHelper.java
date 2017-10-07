package org.elbek.chord.core;

import java.math.BigInteger;

/**
 * Created by elbek on 9/10/17.
 */
public class RingHelper {
    final static int m = 160;
    final static BigInteger[] twoPowers = new BigInteger[m + 1];
    final static BigInteger two = new BigInteger("2");
    static {
        for (int i = 0; i <= m; i++) {
            twoPowers[i] = two.pow(i);
        }
    }
    final static BigInteger modulo = twoPowers[m];
}
