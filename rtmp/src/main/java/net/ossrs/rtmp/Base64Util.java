package net.ossrs.rtmp;

import android.util.Base64;

public class Base64Util {
    public static byte[] encode(String s) {
        return Base64.decode(s, Base64.NO_PADDING);
    }

    public static byte[] encode(byte[] b) {
        return Base64.encode(b, Base64.NO_PADDING);
    }

    public static String encodeToString(byte[] byteArray) {
        return Base64.encodeToString(byteArray, Base64.NO_PADDING);
    }

    public static byte[] decode(String s) {
       return Base64.decode(s, Base64.NO_PADDING);
    }
}
