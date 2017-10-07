package org.elbek.chord.core;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by elbek on 9/10/17.
 */
public class HashUtil {

    public static byte[] SHA1(String text) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("UTF8"), 0, text.length());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null; //TODO
        }
        return md.digest();
    }
}
