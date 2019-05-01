
package com.chamika.research.smartprediction.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.chamika.research.smartprediction.prediction.AppPrediction;
import com.chamika.research.smartprediction.prediction.CallPrediction;
import com.chamika.research.smartprediction.prediction.Event;
import com.chamika.research.smartprediction.prediction.MessagePrediction;
import com.chamika.research.smartprediction.prediction.Prediction;
import com.chamika.research.smartprediction.prediction.PredictionEngine;
import com.chamika.research.smartprediction.ui.hover.MultiSectionHoverMenu;
import com.chamika.research.smartprediction.ui.hover.adapters.OnItemSelectListener;
import com.chamika.research.smartprediction.util.Config;
import com.chamika.research.smartprediction.util.Constant;
import com.chamika.research.smartprediction.util.SettingsUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.mattcarroll.hover.HoverView;
import io.mattcarroll.hover.OnExitListener;
import io.mattcarroll.hover.SideDock;
import io.mattcarroll.hover.overlay.OverlayPermission;
import io.mattcarroll.hover.window.WindowViewController;

public class PredictionService extends Service implements OnItemSelectListener<Prediction> {

    public static final String INTENT_EXTRA_PREDICTIONS = "predictions";
    public static final String INTENT_EXTRA_SCREEN_ON = "screenOn";
    public static final String INTENT_EXTRA_SCREEN_EVENT = "event";
    public static final String INTENT_EXTRA_STOP = "stop";
    public static final String INTENT_EXTRA_REFRESH_PREDICTIONS = "refresh_predictions";
    private static final String TAG = PredictionService.class.getSimpleName();
    private static final int REQUEST_CODE_DATA_COLLECTION_ALARM = 3000;
    private static final int REQUEST_CODE_DB_UPLOAD_ALARM = 3001;
    private static final int REQUEST_CODE_PREDICTION_ENGINE_REFRESH_ALARM = 3002;
    private final BroadcastReceiver screenReceiver = new ScreenReceiver();
    private PredictionEngine predictionEngine;
    private boolean screenReceiverRegistered = false;

    private HoverView mHoverView;
    private boolean mIsRunning = false;
    private OnExitListener mOnMenuOnExitListener = new OnExitListener() {
        public void onExit() {
            Log.d("HoverMenuService", "Menu exit requested. Exiting.");
//            PredictionService.this.mHoverView.removeFromWindow();
//            PredictionService.this.onHoverMenuExitingByUserRequest();
//            PredictionService.this.stopSelf();
        }
    };

    public void onCreate() {
        Log.d("HoverMenuService", "onCreate()");
        startNotification();
    }

    public void startNotification() {
        Notification foregroundNotification = this.getForegroundNotification();
        if (null != foregroundNotification) {
            int notificationId = this.getForegroundNotificationId();
            this.startForeground(notificationId, foregroundNotification);
        } else {
            final String NOTIFICATION_CHANNEL_ID = "com.chamika.research.smartprediction.service.running";
            Notification notification;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelName = "Service Status";
                NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
                chan.setLightColor(Color.BLUE);
                chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                assert manager != null;
                manager.createNotificationChannel(chan);
                Notification.Builder builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
//                        .setContentTitle(getString(R.string.app_name))
//                        .setContentText("Smart Predictions Running...")
                        .setAutoCancel(true);
                notification = builder.build();
            } else {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//                    .setContentTitle(getString(R.string.app_name) + " Running...")
//                    .setContentText("Smart Predictions Running...")
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setAutoCancel(true);
                notification = builder.build();
            }

            this.startForeground(1, notification);
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("HoverMenuService", "onStartCommand() ");
        if (intent != null && intent.hasExtra(INTENT_EXTRA_STOP)) {
            stopPredictionService();
            return Service.START_NOT_STICKY;
        }
        if (!OverlayPermission.hasRuntimePermissionToDrawOverlay(this.getApplicationContext())) {
            Log.e("HoverMenuService", "Cannot display a Hover menu in a Window without the draw overlay permission.");
            this.stopSelf();
            return Service.START_NOT_STICKY;
        } else {
            if (!this.mIsRunning || intent == null) {
                //initial service start or starting after kill
                if (intent != null && intent.hasExtra(INTENT_EXTRA_REFRESH_PREDICTIONS)) {
                    //refresh call should be skipped
                    stopPredictionService();
                    return Service.START_NOT_STICKY;
                }

                initPredictionEngine();
                this.initHoverMenu();
                this.mIsRunning = true;
                if (intent != null) {
                    createMenu(intent);
                }
                startDataCollection();
                scheduleDatabaseUpload(this);
                schedulePredictionEngineRefresh(this);
            } else {
                if (intent.hasExtra(INTENT_EXTRA_SCREEN_ON)) {
                    boolean screenOn = intent.getBooleanExtra(INTENT_EXTRA_SCREEN_ON, false);
                    if (screenOn) {
                        List<Prediction> predictions = predictionEngine.addEventSynchronous(new Event());
                        if (predictions != null) {
                            showPredictions(predictions);
                        }
                    }
                } else if (intent.hasExtra(INTENT_EXTRA_PREDICTIONS)) {
                    createMenu(intent);
                } else if (intent.hasExtra(INTENT_EXTRA_SCREEN_EVENT)) {
                    Event event = (Event) intent.getSerializableExtra(INTENT_EXTRA_SCREEN_EVENT);
                    if (event != null) {
                        showPredictions(predictionEngine.addEventSynchronous(event));
                    }
                } else if (intent.hasExtra(INTENT_EXTRA_REFRESH_PREDICTIONS)) {
                    predictionEngine.refresh();
                }
            }
            registerScreenState();

            return Service.START_STICKY;
        }
    }

    private void stopPredictionService() {
        stopDataCollection();
        stopPredictionEngineRefresh(this);
        stopForeground(true);
        stopSelf();
    }

    public void onDestroy() {
        Log.d("HoverMenuService", "onDestroy()");
        if (this.mIsRunning) {
            this.mHoverView.removeFromWindow();
            this.mIsRunning = false;
        }
        if (this.predictionEngine != null) {
//            this.predictionEngine.removePredictionListener(this);
        }
        unregisterScreenState();

        //Send broadcast to restart this service
//        Intent broadcastIntent = new Intent(this, AppRestartReceiver.class);
//        sendBroadcast(broadcastIntent);
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startDataCollection() {
        Context context = this;

        //ACTIVITY, LOCATION
        this.startService(new Intent(context.getApplicationContext(), UserActivityCollectorService.class));
        Log.d(TAG, "Started activity collection");

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            //SMS,CALL, APPS
            Intent alarmIntent = new Intent(context.getApplicationContext(), ScheduleDataCollectorService.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_DATA_COLLECTION_ALARM, alarmIntent, 0);
            int interval = Config.DATA_COLLECTION_REFRESH_INTERVAL;
            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
            Log.d(TAG, "Started APP, SMS and CALL collection");
        } else {
            Log.d(TAG, "Alarm manager is null");
        }
    }

    private void stopDataCollection() {
        Context context = this;

        //ACTIVITY, LOCATION
        this.stopService(new Intent(context.getApplicationContext(), UserActivityCollectorService.class));
        Log.d(TAG, "Stopped activity collection");

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            //SMS,CALL, APPS
            Intent alarmIntent = new Intent(context.getApplicationContext(), ScheduleDataCollectorService.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_DATA_COLLECTION_ALARM, alarmIntent, 0);
            manager.cancel(pendingIntent);
            Log.d(TAG, "Stopped APP, SMS and CALL collection");
        } else {
            Log.d(TAG, "Alarm manager is null");
        }
    }

    private void scheduleDatabaseUpload(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            Intent alarmIntent = new Intent(context, DataUploaderService.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_DB_UPLOAD_ALARM, alarmIntent, 0);
            int interval = Config.DATA_UPLOAD_INTERVAL;
            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, interval, pendingIntent);
            Log.d(TAG, "Scheduled uploading data");
        }
    }

    private void schedulePredictionEngineRefresh(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            Intent alarmIntent = new Intent(context, PredictionService.class);
            alarmIntent.putExtra(INTENT_EXTRA_REFRESH_PREDICTIONS, true);
            PendingIntent pendingIntent = PendingIntent.getService(context, REQUEST_CODE_PREDICTION_ENGINE_REFRESH_ALARM, alarmIntent, 0);
            int interval = Config.PREDICTION_REFRESH_INTERVAL;

            //trigger at 0100AM
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR, 1);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.add(Calendar.DATE, 1);//next date

            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval, pendingIntent);
            Log.d(TAG, "Scheduled prediction refresh");

            boolean first = SettingsUtil.getBooleanPref(this, Constant.PREF_FIRST, true);
            if (first) {
                //let some time to collect data initially and then refresh engine.
                TimerTask task = new TimerTask() {

                    @Override
                    public void run() {
                        predictionEngine.refresh();
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 10 * 60 * 1000);//trigger after 10mins
                SettingsUtil.setBooleanPref(this, Constant.PREF_FIRST, false);
            }
        }
    }

    private void stopPredictionEngineRefresh(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            Intent alarmIntent = new Intent(context, PredictionService.class);
            alarmIntent.putExtra(INTENT_EXTRA_REFRESH_PREDICTIONS, true);
            PendingIntent pendingIntent = PendingIntent.getService(context, REQUEST_CODE_PREDICTION_ENGINE_REFRESH_ALARM, alarmIntent, 0);
            manager.cancel(pendingIntent);
            Log.d(TAG, "Stopped prediction refresh");
        }
    }

    private void registerScreenState() {
        if (!screenReceiverRegistered) {
            final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(screenReceiver, filter);
            screenReceiverRegistered = true;
            Log.i(TAG, "Screen state registered");
        }
    }

    private void unregisterScreenState() {
        if (screenReceiverRegistered) {
            unregisterReceiver(screenReceiver);
            screenReceiverRegistered = false;
            Log.i(TAG, "Screen state unregistered");
        }
    }


    private void initHoverMenu() {
        this.mHoverView = HoverView.createForWindow(this, new WindowViewController((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)), new SideDock.SidePosition(SideDock.SidePosition.RIGHT, 0.5F));
        this.mHoverView.setOnExitListener(this.mOnMenuOnExitListener);
        this.mHoverView.addToWindow();
    }

    protected Context getContextForHoverMenu() {
        return this;
    }

    @NonNull
    protected HoverView getHoverView() {
        return this.mHoverView;
    }

    protected int getForegroundNotificationId() {
        return 123456789;
    }

    @Nullable
    protected Notification getForegroundNotification() {
        return null;
    }

//    protected void onHoverMenuLaunched(@NonNull Intent intent, @NonNull HoverView hoverView) {
//    }

    protected void onHoverMenuExitingByUserRequest() {
    }

    private void initPredictionEngine() {
        if (predictionEngine == null) {
            this.predictionEngine = new PredictionEngine(this.getApplicationContext());
        }
//        this.predictionEngine.addPredictionListener(this);
    }

    private void createMenu(@NonNull Intent intent) {
        if (intent.hasExtra(INTENT_EXTRA_PREDICTIONS)) {
            showPredictions((List<Prediction>) intent.getSerializableExtra(INTENT_EXTRA_PREDICTIONS));
        }
    }

    private void showPredictions(List<Prediction> predictions) {
        if (predictions != null && !predictions.isEmpty()) {
            MultiSectionHoverMenu menu = new MultiSectionHoverMenu(this, this);
            menu.updateSections(predictions);
            if (menu.getSectionCount() > 0) {
                getHoverView().removeFromWindow();
                initHoverMenu();
                getHoverView().setMenu(menu);
                getHoverView().collapse();
            }
        }
    }

//    @Override
//    public void onPredictions(List<Prediction> predictions) {
//
//        Intent intent = new Intent(this.getApplicationContext(), PredictionService.class);
//        intent.putExtra(INTENT_EXTRA_PREDICTIONS, new ArrayList<Prediction>(predictions));
//        startService(intent);
//    }

    @Override
    public void onItemSelect(View v, Prediction item) {
        Context context = this;
        getHoverView().collapse();
        if (Prediction.Type.APP == item.getType()) {
            AppPrediction appPrediction = (AppPrediction) item;
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appPrediction.getPackageName());
            if (launchIntent != null) {
                context.startActivity(launchIntent);
            } else {
                Toast.makeText(context, "No launcher found for " + appPrediction.getAppName(), Toast.LENGTH_SHORT).show();
            }
        } else if (Prediction.Type.SMS == item.getType()) {
            MessagePrediction messagePrediction = (MessagePrediction) item;
            //prompt to send SMS to the given number
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", messagePrediction.getNumber(), null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Log.d(TAG, "Prompt to send SMS to " + messagePrediction.getNumber());
        } else if (Prediction.Type.CALL == item.getType()) {
            CallPrediction callPrediction = (CallPrediction) item;
            //prompt to call to the given number
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", callPrediction.getNumber(), null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Log.d(TAG, "Prompt to call to " + callPrediction.getNumber());
        }
    }
}