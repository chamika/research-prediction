package com.chamika.research.smartprediction.service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.chamika.research.smartprediction.util.Config;
import com.chamika.research.smartprediction.util.Constant;
import com.chamika.research.smartprediction.util.SettingsUtil;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.awareness.snapshot.HeadphoneStateResult;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.awareness.snapshot.PlacesResult;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.places.PlaceLikelihood;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chamika on 9/11/16.
 */

public class BackgroundService extends Service {

    private static final String TAG = BackgroundService.class.getSimpleName();

    private static Timer timer;
    private final IBinder binder = new LocalBinder();
    private boolean started = false;
    private GoogleApiClient client;
    //    private final BroadcastReceiver screenReceiver = new ScreenReceiver();
    private PendingIntent fencePendingIntent;
    private FenceReceiver fenceReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void startDetecting() {
        fenceReceiver = new FenceReceiver();
        registerReceiver(fenceReceiver, new IntentFilter(Constant.FENCE_RECEIVER_ACTION));

        Intent intent = new Intent(Constant.FENCE_RECEIVER_ACTION);
        fencePendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, 0);

        if (SettingsUtil.getBooleanPref(this, Constant.PREF_ACTIVITY)) {
            for (int enabledActivity : Config.ENABLED_ACTIVITIES) {
                registerFence(Constant.FENCE_ACTIVITY + enabledActivity, DetectedActivityFence.starting(enabledActivity));
            }
        } else {
            Log.d(TAG, "Activity sync disabled.");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (client == null) {
            client = new GoogleApiClient.Builder(this)
                    .addApi(Awareness.API)
                    .build();
            client.connect();
            registerScreenON();
        }
        if (!started) {
//            startSensorReadingSnapshot();
            startDetecting();
            Log.d(TAG, "activity detection background service started");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSensorReadingSnapshot();
        unregisterScreenON();
        for (int enabledActivity : Config.ENABLED_ACTIVITIES) {
            try {
                unregisterFence(Constant.FENCE_ACTIVITY + enabledActivity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        unregisterReceiver(fenceReceiver);
        Log.d(TAG, "activity detection background service stopped");
    }

    private void startSensorReadingSnapshot() {
        started = true;
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new SnapshopRetriever(), 0, Config.SERVICE_REFRESH_INTERVAL);
    }

    private void stopSensorReadingSnapshot() {
        if (started) {
            timer.cancel();
        }
        started = false;
    }

    private void getSnapshotUpdate() {
        Awareness.SnapshotApi.getDetectedActivity(client)
                .setResultCallback(new ResultCallback<DetectedActivityResult>() {
                    @Override
                    public void onResult(@NonNull DetectedActivityResult detectedActivityResult) {
                        if (!detectedActivityResult.getStatus().isSuccess()) {
                            Log.e("MainActivity", "Could not get the current activity.");
                            Log.d(TAG, "Could not get the current activity.");
                            return;
                        }
                        ActivityRecognitionResult ar = detectedActivityResult.getActivityRecognitionResult();
                        DetectedActivity probableActivity = ar.getMostProbableActivity();
                        Log.i("MainActivity", probableActivity.toString());
                        Log.d(TAG, probableActivity.toString() + " at " + getTimeText());
//                        contextBundler.addContext(ar);
                    }
                });

        Awareness.SnapshotApi.getHeadphoneState(client).setResultCallback(new ResultCallback<HeadphoneStateResult>() {
            @Override
            public void onResult(@NonNull HeadphoneStateResult headphoneStateResult) {
                if (!headphoneStateResult.getStatus().isSuccess()) {
                    Log.d(TAG, "Could not get the headphone state.");
                    return;
                }
                HeadphoneState headphoneState = headphoneStateResult.getHeadphoneState();
                String state = "";
                if (headphoneState.getState() == HeadphoneState.PLUGGED_IN) {
                    state = "PLUGGED_IN";
                } else if (headphoneState.getState() == HeadphoneState.UNPLUGGED) {
                    state = "UNPLUGGED";
                }
                Log.d(TAG, state + " at " + getTimeText());
//                contextBundler.addContext(headphoneStateResult);
            }
        });

        //Already granted permission at app startup
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Awareness.SnapshotApi.getLocation(client).setResultCallback(new ResultCallback<LocationResult>() {
            @Override
            public void onResult(@NonNull LocationResult locationResult) {
                if (!locationResult.getStatus().isSuccess()) {
                    Log.d(TAG, "Could not get location");
                    return;
                }
                Log.d(TAG, locationResult.getLocation().toString() + " at " + getTimeText());
            }
        });
        Awareness.SnapshotApi.getPlaces(client).setResultCallback(new ResultCallback<PlacesResult>() {
            @Override
            public void onResult(@NonNull PlacesResult placesResult) {
                if (!placesResult.getStatus().isSuccess()) {
                    Log.d(TAG, "Could not get place result");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                if (placesResult.getPlaceLikelihoods() != null) {
                    for (PlaceLikelihood hood : placesResult.getPlaceLikelihoods()) {
                        sb.append(hood.getPlace().getName());
                        sb.append(',');
                    }
                } else {
                    sb.append("No places found");
                }
                Log.d(TAG, sb.toString() + " at " + getTimeText());
//                contextBundler.addContext(placesResult);
            }
        });


        Awareness.SnapshotApi.getWeather(client).setResultCallback(new ResultCallback<WeatherResult>() {
            @Override
            public void onResult(@NonNull WeatherResult weatherResult) {
                if (!weatherResult.getStatus().isSuccess()) {
                    Log.d(TAG, "Could not get weather");
                    return;
                }
                Weather weather = weatherResult.getWeather();
                if (weather != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Temperature:");
                    sb.append(weather.getTemperature(Weather.CELSIUS));
                    sb.append("`C, Feels like:");
                    sb.append(weather.getFeelsLikeTemperature(Weather.CELSIUS));
                    sb.append("`C, Dew Point:");
                    sb.append(weather.getDewPoint(Weather.CELSIUS));
                    sb.append("`C, Humidiy:");
                    sb.append(weather.getHumidity());
                    sb.append(", Conditions:");

                    int[] conditions = weather.getConditions();
                    for (int val : conditions) {
                        try {
                            sb.append(getVariableName("CONDITION_", Weather.class, val));
                            sb.append(", ");
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(TAG, sb.toString() + " at " + getTimeText());
//                    contextBundler.addContext(weatherResult);
                } else {
                    Log.d(TAG, "No weather data found at " + getTimeText());
                }
            }
        });


    }

    private String getVariableName(String prefix, Class clazz, Object value) throws IllegalAccessException {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getName().startsWith(prefix) && value.equals(field.get(null))) {
                return field.getName().replace(prefix, "");
            }
        }
        return "";
    }


    private String getTimeText() {
        return new SimpleDateFormat("hh:mm:ss").format(new Date());
    }

    private void registerScreenON() {
//        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
//        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        registerReceiver(screenReceiver, filter);
    }

    private void unregisterScreenON() {
//        unregisterReceiver(screenReceiver);
    }

    protected void registerFence(final String fenceKey, final AwarenessFence fence) {
        Awareness.FenceApi.updateFences(
                client,
                new FenceUpdateRequest.Builder()
                        .addFence(fenceKey, fence, fencePendingIntent)
                        .build())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Fence was successfully registered. key =" + fenceKey);
//                            queryFence(fenceKey);
                        } else {
                            Log.e(TAG, "Fence could not be registered: " + status);
                        }
                    }
                });
    }

    protected void unregisterFence(final String fenceKey) {
        Awareness.FenceApi.updateFences(
                client,
                new FenceUpdateRequest.Builder()
                        .removeFence(fenceKey)
                        .build()).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                Log.i(TAG, "Fence " + fenceKey + " successfully removed.");
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Log.i(TAG, "Fence " + fenceKey + " could NOT be removed.");
            }
        });
    }

    private class SnapshopRetriever extends TimerTask {

        @Override
        public void run() {
            getSnapshotUpdate();
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public BackgroundService getService() {
            // Return this instance of BackgroundService so clients can call public methods
            return BackgroundService.this;
        }
    }

    class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                stopSensorReadingSnapshot();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                startSensorReadingSnapshot();
            }
        }
    }
}


