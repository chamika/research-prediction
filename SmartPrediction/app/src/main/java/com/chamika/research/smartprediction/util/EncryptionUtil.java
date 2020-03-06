package com.chamika.research.smartprediction.util;

import android.content.Context;
import android.provider.Settings;
import android.util.Base64;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtil {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    //    private static final String KEY = "4xMrL7oXb7TWsJMb";
    private static final String AES_CTR_NO_PADDING = "AES/CTR/NoPadding";
    private static final int IV_LENGTH = 16;
    private static final byte[] IV = "NtArdEmETrestAMB".getBytes();

    public static String encrypt(Context context, String input) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        byte[] iv = IV;

        byte[] key = getKey(context).getBytes(DEFAULT_CHARSET);
        SecretKey secretKeySpec = new SecretKeySpec(key, "AES");

        final Cipher cipher = Cipher.getInstance(AES_CTR_NO_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        byte[] cipherText = cipher.doFinal(input.getBytes(DEFAULT_CHARSET));

        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);
        byte[] cipherMessage = byteBuffer.array();

        return encodeBase64(cipherMessage);
    }

    public static String decrypt(Context context, String input) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(decodeBase64(input));
        byte[] iv = new byte[IV_LENGTH];
        byteBuffer.get(iv);
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        byte[] key = getKey(context).getBytes(DEFAULT_CHARSET);

        final Cipher cipher = Cipher.getInstance(AES_CTR_NO_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        byte[] decryptedBytes = cipher.doFinal(cipherText);

        return new String(decryptedBytes, DEFAULT_CHARSET);
    }

    private static String encodeBase64(byte[] encryptBytes) {
        return Base64.encodeToString(encryptBytes, Base64.NO_WRAP);
    }

    private static byte[] decodeBase64(String text) {
        return Base64.decode(text, Base64.NO_WRAP);
    }

    private static String getKey(Context context) {
        String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        id = md5(id);
        id = id.substring(0, Math.min(16, id.length()));
        return id;
    }

    private static String md5(String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return s;
        }
    }
}