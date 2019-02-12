package com.chamika.research.smartprediction.util;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.chamika.research.smartprediction.store.BaseStore;

import me.everything.providers.android.telephony.Sms;
import me.everything.providers.android.telephony.TelephonyProvider;
import me.everything.providers.core.Data;

/**
 * Created by chamika on 3/16/17.
 */

public class SMSUtil {

    public static final String SMS = "SMS";
    public static final String TAG = SMSUtil.class.getSimpleName();

    public static void getSMS(Context context) {
        if (!SettingsUtil.getBooleanPref(context, Constant.PREF_MSG)) {
            Log.d(TAG, "SMS sync disabled.");
            return;
        }
        TelephonyProvider provider = new TelephonyProvider(context);
        Data<Sms> sms = provider.getSms(TelephonyProvider.Filter.ALL);
        Cursor cursor = sms.getCursor();
        long lastTime = SettingsUtil.getTime(context, "SMS");
        long maxTime = lastTime;
        int count = 0;
        if (cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                Sms msg = sms.fromCursor(cursor);
                if (lastTime < msg.receivedDate) {
                    Log.d(TAG, "SMS:" + msg.toString());
                    int actionType = (msg.type == Sms.MessageType.SENT) ? 1 : 2;
                    BaseStore.saveEvent(context, actionType, SMS, msg.receivedDate, StringUtil.maskNumber(msg.address));
                    if (maxTime < msg.receivedDate) {
                        maxTime = msg.receivedDate;
                    }
                    count++;
                }
            }
            SettingsUtil.setTime(context, SMS, maxTime);
        }
        Log.d(TAG, "SMS sync completed." + count + " new msgs added");
    }
}