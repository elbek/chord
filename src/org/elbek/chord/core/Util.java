package org.elbek.chord.core;

import java.math.BigInteger;

/**
 * Created by elbek on 9/10/17.
 */
public class Util {

    /**
     * returns true if target is between start and end, search happen clockwise from start to end, NOT other way around
     *
     * @param start
     * @param end
     * @param target
     * @param startIncluded
     * @param endIncluded
     * @return
     */
    static boolean isInRange(BigInteger start, BigInteger end, BigInteger target, boolean startIncluded, boolean endIncluded) {
        if (end.compareTo(start) > 0) {
            boolean startGood = startIncluded ? target.compareTo(start) >= 0 : target.compareTo(start) > 0;
            boolean endGood = endIncluded ? end.compareTo(target) >= 0 : end.compareTo(target) > 0;
            return startGood && endGood;
        } else {
            boolean startGood = startIncluded ? target.compareTo(start) >= 0 : target.compareTo(start) > 0;
            if (startGood) { //if it is bigger then start, then it must be in the range, since end is smaller than start
                return true;
            }
            return endIncluded ? end.compareTo(target) >= 0 : end.compareTo(target) > 0;  //check if end point is fine, if so return true
        }
    }

    static void prependString(char[] bytes, String data) {
        if (data.length() > bytes.length) {
            return;
        }
        int delta = bytes.length - data.length();
        for (int i = bytes.length - 1; i >= 0; i--) {
            if (i - delta >= 0) {
                bytes[i] = data.charAt(i - delta);
            } else {
                bytes[i] = ' ';
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(isInRange(RingHelper.twoPowers[1], RingHelper.twoPowers[10], RingHelper.twoPowers[2], false, false));
        System.out.println(!isInRange(RingHelper.twoPowers[1], RingHelper.twoPowers[10], RingHelper.twoPowers[12], false, false));
        System.out.println(isInRange(RingHelper.twoPowers[10], RingHelper.twoPowers[5], RingHelper.twoPowers[12], false, false));
        System.out.println(!isInRange(RingHelper.twoPowers[10], RingHelper.twoPowers[11], RingHelper.twoPowers[12], false, false));
        System.out.println(isInRange(RingHelper.twoPowers[10], RingHelper.twoPowers[10], RingHelper.twoPowers[5], false, false));
        System.out.println(!isInRange(RingHelper.twoPowers[10], RingHelper.twoPowers[5], RingHelper.twoPowers[7], false, false));
        System.out.println(!isInRange(RingHelper.twoPowers[10], RingHelper.twoPowers[12], RingHelper.twoPowers[10], false, false));
        System.out.println(!isInRange(RingHelper.twoPowers[10], RingHelper.twoPowers[12], RingHelper.twoPowers[12], false, false));
        System.out.println(isInRange(RingHelper.twoPowers[10], RingHelper.twoPowers[12], RingHelper.twoPowers[10], true, true));
        System.out.println(isInRange(RingHelper.twoPowers[100], RingHelper.twoPowers[12], RingHelper.twoPowers[10], false, false));

        System.out.println(!isInRange(RingHelper.twoPowers[100], RingHelper.twoPowers[12], RingHelper.twoPowers[100], false, true));
        System.out.println(!isInRange(RingHelper.twoPowers[100], RingHelper.twoPowers[12], RingHelper.twoPowers[12], false, false));
        System.out.println(isInRange(RingHelper.twoPowers[100], RingHelper.twoPowers[12], RingHelper.twoPowers[12], false, true));
        System.out.println(isInRange(RingHelper.twoPowers[100], RingHelper.twoPowers[100], RingHelper.twoPowers[12], false, false));
    }
}
