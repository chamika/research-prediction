package com.chamika.research.smartprediction.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.chamika.research.smartprediction.util.Config;


public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
        setupPeriodicalDataCollection(context);
        setupDataUploading(context);
        context.startService(new Intent(context.getApplicationContext(), UserActivityCollectorService.class));
//        }
    }

    private void setupDataUploading(Context context) {
        Intent alarmIntent = new Intent(context, DataUploaderService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int interval = Config.DATA_UPLOAD_INTERVAL;
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 300000, interval, pendingIntent);
    }

    private void setupPeriodicalDataCollection(Context context) {
        Intent alarmIntent = new Intent(context, ScheduleDataCollectorService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int interval = Config.DATA_COLLECTION_REFRESH_INTERVAL;
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
    }
}