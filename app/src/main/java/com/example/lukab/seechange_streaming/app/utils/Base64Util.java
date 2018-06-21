package com.example.lukab.seechange_streaming.app.utils;

import android.util.Base64;

public class Base64Util {
    public static byte[] encode(String s) {
        return Base64.decode(s, android.util.Base64.NO_PADDING);
    }

    public static String encodeToString(byte[] byteArray) {
        return Base64.encodeToString(byteArray, android.util.Base64.NO_PADDING);
    }

    public static byte[] decode(String s) {
       return Base64.decode(s, Base64.NO_PADDING);
    }
}
