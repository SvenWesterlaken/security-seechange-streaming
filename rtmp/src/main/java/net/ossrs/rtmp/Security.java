package net.ossrs.rtmp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;

public class Security {
    private static PrivateKey privateKey;

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte aHash : hash) {
            String hex = Integer.toHexString(0xff & aHash);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String HashData(byte[] data) {
        MessageDigest digest;
        String hexString = "";
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(data);
            hexString = bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hexString;
    }

    public static void setPrivateKey(PrivateKey privateKey) {
        Security.privateKey = privateKey;
    }

    public static PrivateKey getPrivateKey() {
        return Security.privateKey;
    }

}
