package com.example.lukab.seechange_streaming.app.utils;

import android.util.Base64;
import android.util.Log;

import com.example.lukab.seechange_streaming.viewModel.LoginViewModel;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {

    public static RSAPublicKey getPublicKeyFromString(String publicKey)
        throws GeneralSecurityException {
            String publicKeyPEM = publicKey;

            publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----\n", "").replace("\n-----END PUBLIC KEY-----\n", "");

            byte[] encoded = Base64Util.decode(publicKeyPEM);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(encoded));

            return pubKey;
        }

    public static PrivateKey getPrivateKeyFromString(String privateKey)
            throws GeneralSecurityException {
        String privateKeyPEM = privateKey;

        privateKeyPEM = privateKeyPEM
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "");
        Log.d("LoginViewModel: ", "privatePem without begin and end: " + privateKeyPEM);

        byte[] encoded = Base64Util.decode(privateKeyPEM);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPrivateCrtKeySpec rsaKeySpec = null;

        try {
            rsaKeySpec = getRSAKeySpec(encoded);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (rsaKeySpec != null) {
            PrivateKey privKey = kf.generatePrivate(rsaKeySpec);
            return privKey;
        } else {
            return null;
        }
    }

    public static String decryptPrivateKey(String password, String encrypted) throws Exception {
        byte[] key = password.getBytes();
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] thedigest = md.digest(key);
        SecretKeySpec skey = new SecretKeySpec(thedigest, "AES/ECB/PKCS7Padding");
        Cipher dcipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
        dcipher.init(Cipher.DECRYPT_MODE, skey);
        byte[] clearbyte = dcipher.doFinal(toByte(encrypted));
        return new String(clearbyte);
    }

    private static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }

    private static RSAPrivateCrtKeySpec getRSAKeySpec(byte[] keyBytes) throws IOException {

        DerParser parser = new DerParser(keyBytes);

        Asn1Object sequence = parser.read();
        if (sequence.getType() != DerParser.SEQUENCE)
            throw new IOException("Invalid DER: not a sequence"); //$NON-NLS-1$

        // Parse inside the sequence
        parser = sequence.getParser();

        parser.read(); // Skip version
        BigInteger modulus = parser.read().getInteger();
        BigInteger publicExp = parser.read().getInteger();
        BigInteger privateExp = parser.read().getInteger();
        BigInteger prime1 = parser.read().getInteger();
        BigInteger prime2 = parser.read().getInteger();
        BigInteger exp1 = parser.read().getInteger();
        BigInteger exp2 = parser.read().getInteger();
        BigInteger crtCoef = parser.read().getInteger();

        RSAPrivateCrtKeySpec keySpec = new RSAPrivateCrtKeySpec(
                modulus, publicExp, privateExp, prime1, prime2,
                exp1, exp2, crtCoef);

        return keySpec;
    }
}
