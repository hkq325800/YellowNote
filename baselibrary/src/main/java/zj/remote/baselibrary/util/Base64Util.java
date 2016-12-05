package zj.remote.baselibrary.util;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by hkq325800 on 2016/12/5.
 */

public class Base64Util {
    //encode

    private static byte[] sha1(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-1").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] stringToSha1(String str) {
        return /*sha1(*/str.getBytes(Charset.forName("UTF-8"))/*)*/;
    }

    public static String stringToSha1String(String string) {
        return bytesToString(stringToSha1(string));
    }

    private static String bytesToString(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    //decode

    private static byte[] stringToBytes(String string) {
        return Base64.decode(string, Base64.DEFAULT);
    }

    public static String sha1StringToString(String string) {
        try {
            return new String(stringToBytes(string), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
