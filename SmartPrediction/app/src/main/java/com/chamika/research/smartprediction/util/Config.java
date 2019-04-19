package com.chamika.research.smartprediction.util;

import com.google.android.gms.awareness.fence.DetectedActivityFence;

/**
 * Created by chamika on 3/15/17.
 */

public class Config {
    public static final int SERVICE_REFRESH_INTERVAL = 10000;
    public static final String DATA_FILE_NAME = "data.txt";
    // need to update com.chamika.research.datacollector.service.FenceReceiver.onReceive()
    public static final int[] ENABLED_ACTIVITIES = new int[]{DetectedActivityFence.IN_VEHICLE,
            DetectedActivityFence.ON_BICYCLE,
            DetectedActivityFence.ON_FOOT,
            DetectedActivityFence.WALKING,
            DetectedActivityFence.RUNNING};
    public static int DATA_COLLECTION_REFRESH_INTERVAL = 900000;
    public static int DATA_UPLOAD_INTERVAL = 21600000;
    public static int PREDICTION_REFRESH_INTERVAL = 24 * 60 * 60 * 1000; //24 hours

    //TODO set to false when releasing
    public final static boolean DEMO = true;

}
