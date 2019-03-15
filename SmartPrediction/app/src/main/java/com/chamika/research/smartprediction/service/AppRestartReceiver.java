package com.chamika.research.smartprediction.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class AppRestartReceiver extends BroadcastReceiver {
    private static String TAG = AppRestartReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive()");
        Intent serviceIntent = new Intent(context.getApplicationContext(), PredictionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getApplicationContext().startForegroundService(serviceIntent);
        } else {
            context.getApplicationContext().startService(serviceIntent);
        }
    }
}
