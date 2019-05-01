package com.chamika.research.smartprediction.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by chamika on 3/18/17.
 */

public class SettingsUtil {

    public static final String PREF = "pref";

    public static long getTime(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sharedPref.getLong(key, 0);
    }

    public static void setTime(Context context, String key, long value) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static boolean getBooleanPref(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(key, false);
    }

    public static boolean getBooleanPref(Context context, String key, boolean defValue) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(key, defValue);
    }

    public static void setBooleanPref(Context context, String key, boolean value) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
}
