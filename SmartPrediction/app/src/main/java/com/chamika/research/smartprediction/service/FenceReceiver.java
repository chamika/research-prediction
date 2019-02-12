package com.chamika.research.smartprediction.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.chamika.research.smartprediction.store.BaseStore;
import com.chamika.research.smartprediction.util.Constant;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceState;

import java.util.Date;

/**
 * Created by chamika on 3/18/17.
 */

public class FenceReceiver extends BroadcastReceiver {

    private final String TAG = FenceReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        FenceState fenceState = FenceState.extract(intent);

        Log.d(TAG, "Fence received:" + fenceState.getFenceKey() + " state:" + fenceState.getCurrentState());

        if (fenceState.getCurrentState() == FenceState.TRUE) {
            String activity = null;
            switch (fenceState.getFenceKey()) {
                case Constant.FENCE_ACTIVITY + DetectedActivityFence.IN_VEHICLE:
                    activity = "IN_VEHICLE";
                    break;
                case Constant.FENCE_ACTIVITY + DetectedActivityFence.ON_BICYCLE:
                    activity = "ON_BICYCLE";
                    break;
                case Constant.FENCE_ACTIVITY + DetectedActivityFence.ON_FOOT:
                    activity = "ON_FOOT";
                    break;
                case Constant.FENCE_ACTIVITY + DetectedActivityFence.WALKING:
                    activity = "WALKING";
                    break;
                case Constant.FENCE_ACTIVITY + DetectedActivityFence.RUNNING:
                    activity = "RUNNING";
                    break;
            }
            if (activity != null) {
                Log.d(TAG, "Activity detected: " + activity);
                BaseStore.saveEvent(context, 3, "ACT", new Date().getTime(), activity);
            }
        }
    }
}
