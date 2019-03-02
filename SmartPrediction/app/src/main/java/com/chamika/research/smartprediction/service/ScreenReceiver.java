package com.chamika.research.smartprediction.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ScreenReceiver extends BroadcastReceiver {

    private final static String TAG = ScreenReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");

        boolean needEvent = false;
        boolean screenOn = false;
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            screenOn = true;
            needEvent = true;
            Log.i(TAG, "Screen ON");
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            screenOn = false;
            needEvent = true;
            Log.i(TAG, "Screen OFF");
        }
        if (needEvent) {
            Intent i = new Intent(context, PredictionHoverMenuService.class);
            i.putExtra(PredictionHoverMenuService.INTENT_EXTRA_SCREEN_ON, screenOn);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i);
            } else {
                context.startService(i);
            }
        }
    }
}

