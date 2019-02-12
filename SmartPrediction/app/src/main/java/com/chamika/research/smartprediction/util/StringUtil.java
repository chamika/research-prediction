package com.chamika.research.smartprediction.util;

/**
 * Created by chamika on 3/19/17.
 */

public class StringUtil {
    public static String maskNumber(String number) {
        int hashCode = number.hashCode();
        return Integer.toHexString(hashCode);
    }
}
