package com.chamika.research.smartprediction.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.chamika.research.smartprediction.util.AppUsageUtil;
import com.chamika.research.smartprediction.util.CallsUtils;
import com.chamika.research.smartprediction.util.SMSUtil;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by chamika on 3/12/17.
 */

public class ScheduleDataCollectorService extends BroadcastReceiver {

    private static final String TAG = ScheduleDataCollectorService.class.getSimpleName();

    private GoogleApiClient client;

    public ScheduleDataCollectorService() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            SMSUtil.getSMS(context);
        } catch (Exception e) {
            Log.e(TAG, "Error in updating SMS", e);
        }
        try {
            CallsUtils.getCalls(context);
        } catch (Exception e) {
            Log.e(TAG, "Error in updating Calls", e);
        }
        try {
            AppUsageUtil.updateAppUsage(context);
        } catch (Exception e) {
            Log.e(TAG, "Error in updating App usage", e);
        }
    }
}
