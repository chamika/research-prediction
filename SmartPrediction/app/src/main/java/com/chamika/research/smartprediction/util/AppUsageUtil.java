package com.chamika.research.smartprediction.util;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import com.chamika.research.smartprediction.store.BaseStore;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by chamika on 3/16/17.
 */

public class AppUsageUtil {

    public static final String APP = "APP";
    public static final String TAG = AppUsageUtil.class.getSimpleName();

    public static void updateAppUsage(Context context) {
        if (!SettingsUtil.getBooleanPref(context, Constant.PREF_APP_USAGE)) {
            Log.d(TAG, "App Usage sync disabled.");
            return;
        }

        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            Log.d(TAG, "App Usage sync failed. UsageStatsManager not found");
            return;
        }

        Calendar cal = Calendar.getInstance();
        long time = SettingsUtil.getTime(context, APP);
        if (time == 0) {
            cal.add(Calendar.DATE, -30);
        } else {
            cal.setTimeInMillis(time);
        }

        long syncTime = System.currentTimeMillis();

        UsageEvents usageEvents = usageStatsManager.queryEvents(cal.getTimeInMillis(), syncTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int count = 0;
        int countAll = 0;
        while (usageEvents.hasNextEvent()) {
            countAll++;
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);
            if (event.getEventType() != UsageEvents.Event.MOVE_TO_BACKGROUND) {
                boolean hasLauncher = context.getPackageManager().getLaunchIntentForPackage(event.getPackageName()) != null;
                if (hasLauncher) {
//                    Log.d(TAG, "App Event:" + event.getPackageName() + " " + getEventType(event.getEventType()) + " " + sdf.format(new Date(event.getTimeStamp())));
                    BaseStore.saveEvent(context, 1, APP, event.getTimeStamp(), event.getPackageName(), null);
                    count++;
                }
            }
        }
        SettingsUtil.setTime(context, APP, syncTime);

//        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, cal.getTimeInMillis(),
//                syncTime);
//        int count = 0;
//        if (queryUsageStats != null && !queryUsageStats.isEmpty()) {
//            for (UsageStats stat : queryUsageStats) {
//                if (cal.getTimeInMillis() < stat.getFirstTimeStamp()) {
//                    Log.d(TAG, "APP:" + stat.getPackageName());
//                    if (stat.getTotalTimeInForeground() > 0) {
//                        int actionType = 1;
//                        BaseStore.saveEvent(context, actionType, APP, stat.getLastTimeUsed(), stat.getPackageName(), String.valueOf(stat.getTotalTimeInForeground()));
//                        count++;
//                    }
//                }
//            }
//            SettingsUtil.setTime(context, APP, syncTime);
//        }

        Log.d(TAG, "App usage  sync completed. " + countAll + " entries found. " + count + " new entries added");
    }

    private static String getEventType(int type) {
        switch (type) {
            case 0:
                return "NONE";
            case 1:
                return "MOVE_TO_FOREGROUND";
            case 2:
                return "MOVE_TO_BACKGROUND";
            case 3:
                return "END_OF_DAY";
            case 4:
                return "CONTINUE_PREVIOUS_DAY";
            case 5:
                return "CONFIGURATION_CHANGE";
            case 6:
                return "SYSTEM_INTERACTION";
            case 7:
                return "USER_INTERACTION";
            case 8:
                return "SHORTCUT_INVOCATION";
            case 9:
                return "CHOOSER_ACTION";
            default:
                return "UNKNOWN(" + String.valueOf(type) + ")";
        }
    }
}