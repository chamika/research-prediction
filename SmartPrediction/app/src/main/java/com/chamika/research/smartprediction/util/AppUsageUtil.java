package com.chamika.research.smartprediction.util;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import com.chamika.research.smartprediction.store.BaseStore;

import java.util.Calendar;
import java.util.List;

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

        Calendar cal = Calendar.getInstance();
        long time = SettingsUtil.getTime(context, APP);
        if (time == 0) {
            cal.add(Calendar.DATE, -30);
        } else {
            cal.setTimeInMillis(time);
        }

        long syncTime = System.currentTimeMillis();
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, cal.getTimeInMillis(),
                syncTime);

        int count = 0;
        if (queryUsageStats != null && !queryUsageStats.isEmpty()) {
            for (UsageStats stat : queryUsageStats) {
                if (cal.getTimeInMillis() < stat.getFirstTimeStamp()) {
                    Log.d(TAG, "APP:" + stat.getPackageName());
                    if (stat.getTotalTimeInForeground() > 0) {
                        int actionType = 1;
                        BaseStore.saveEvent(context, actionType, APP, stat.getLastTimeUsed(), stat.getPackageName(), String.valueOf(stat.getTotalTimeInForeground()));
                        count++;
                    }
                }
            }
            SettingsUtil.setTime(context, APP, syncTime);
        }

        Log.d(TAG, "App usage  sync completed. " + queryUsageStats.size() + " entries found. " + count + " new entries added");
    }
}