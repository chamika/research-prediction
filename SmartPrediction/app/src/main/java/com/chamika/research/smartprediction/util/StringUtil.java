package com.chamika.research.smartprediction.util;

import android.content.Context;

/**
 * Created by chamika on 3/19/17.
 */

public class StringUtil {
    public static String maskNumber(String number) {
        int hashCode = number.hashCode();
        return Integer.toHexString(hashCode);
    }

    public static String encrypt(Context context, String text) {
        try {
            return EncryptionUtil.encrypt(context, text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }

    public static String decrypt(Context context, String text) {
        try {
            return EncryptionUtil.decrypt(context, text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }
}
