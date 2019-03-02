package com.chamika.research.smartprediction.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AppRestartReceiver extends BroadcastReceiver {
    private static String TAG = AppRestartReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive()");
        context.getApplicationContext().startService(new Intent(context.getApplicationContext(), PredictionHoverMenuService.class));
    }
}
