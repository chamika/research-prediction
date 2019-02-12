
package com.chamika.research.smartprediction.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.chamika.research.smartprediction.prediction.AppPrediction;
import com.chamika.research.smartprediction.prediction.Event;
import com.chamika.research.smartprediction.prediction.Prediction;
import com.chamika.research.smartprediction.prediction.PredictionEngine;
import com.chamika.research.smartprediction.prediction.PredictionListener;
import com.chamika.research.smartprediction.ui.hover.MultiSectionHoverMenu;
import com.chamika.research.smartprediction.ui.hover.adapters.OnItemSelectListener;

import java.util.ArrayList;
import java.util.List;

import io.mattcarroll.hover.HoverMenu;
import io.mattcarroll.hover.HoverView;
import io.mattcarroll.hover.OnExitListener;
import io.mattcarroll.hover.SideDock;
import io.mattcarroll.hover.overlay.OverlayPermission;
import io.mattcarroll.hover.window.WindowViewController;

public class PredictionHoverMenuService extends Service implements PredictionListener, OnItemSelectListener<Prediction> {

    public static final String INTENT_EXTRA_PREDICTIONS = "predictions";
    public static final String INTENT_EXTRA_SCREEN_ON = "screenOn";
    private static final String TAG = PredictionHoverMenuService.class.getSimpleName();
    private final BroadcastReceiver screenReceiver = new ScreenReceiver();
    private PredictionEngine predictionEngine;
    private boolean screenReceiverRegistered = false;

    private HoverView mHoverView;
    private boolean mIsRunning = false;
    private OnExitListener mOnMenuOnExitListener = new OnExitListener() {
        public void onExit() {
            Log.d("HoverMenuService", "Menu exit requested. Exiting.");
//            PredictionHoverMenuService.this.mHoverView.removeFromWindow();
//            PredictionHoverMenuService.this.onHoverMenuExitingByUserRequest();
//            PredictionHoverMenuService.this.stopSelf();
        }
    };

    public void onCreate() {
        Log.d("HoverMenuService", "onCreate()");
        Notification foregroundNotification = this.getForegroundNotification();
        if (null != foregroundNotification) {
            int notificationId = this.getForegroundNotificationId();
            this.startForeground(notificationId, foregroundNotification);
        }

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!OverlayPermission.hasRuntimePermissionToDrawOverlay(this.getApplicationContext())) {
            Log.e("HoverMenuService", "Cannot display a Hover menu in a Window without the draw overlay permission.");
            this.stopSelf();
            return Service.START_NOT_STICKY;
        } else {
            if (!this.mIsRunning || intent == null) {
                //initial service start or starting after kill
                Log.d("HoverMenuService", "onStartCommand() ");
                initPredictionEngine();
                this.initHoverMenu();
                this.mIsRunning = true;
                if (intent != null) {
                    createMenu(intent);
                }
            } else {
                if (intent.hasExtra(INTENT_EXTRA_SCREEN_ON)) {
                    boolean screenOn = intent.getBooleanExtra(INTENT_EXTRA_SCREEN_ON, false);
                    if (screenOn) {
                        predictionEngine.addEvent(new Event());
                    }
                } else if (intent.hasExtra(INTENT_EXTRA_PREDICTIONS)) {
                    createMenu(intent);
                }
            }
            registerScreenState();

            return Service.START_STICKY;
        }
    }

    public void onDestroy() {
        Log.d("HoverMenuService", "onDestroy()");
        if (this.mIsRunning) {
            this.mHoverView.removeFromWindow();
            this.mIsRunning = false;
        }
        if (this.predictionEngine != null) {
            this.predictionEngine.removePredictionListener(this);
        }
        unregisterScreenState();

    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
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
        this.mHoverView = HoverView.createForWindow(this, new WindowViewController((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)), new SideDock.SidePosition(1, 0.5F));
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
        this.predictionEngine = PredictionEngine.getInstance(this.getApplicationContext());
        this.predictionEngine.addPredictionListener(this);
    }

    private void createMenu(@NonNull Intent intent) {
        HoverView hoverView = getHoverView();
        if (intent.hasExtra(INTENT_EXTRA_PREDICTIONS)) {
            HoverMenu menu = new MultiSectionHoverMenu(this, (List<Prediction>) intent.getSerializableExtra(INTENT_EXTRA_PREDICTIONS), this);
            hoverView.setMenu(menu);
            hoverView.collapse();
        }
    }

    @Override
    public void onPredictions(List<Prediction> predictions) {

        Intent intent = new Intent(this.getApplicationContext(), PredictionHoverMenuService.class);
        intent.putExtra(INTENT_EXTRA_PREDICTIONS, new ArrayList<Prediction>(predictions));
        startService(intent);

//        HoverView hoverView = HoverView.createForWindow(this,
//                new WindowViewController((WindowManager) getSystemService(Context.WINDOW_SERVICE))
//        );
//        HoverMenu menu = new SingleSectionHoverMenu(this);
//        hoverView.setMenu(menu);
//        hoverView.addToWindow();
//        hoverView.collapse();
    }

    @Override
    public void onItemSelect(View v, Prediction item) {
        Context context = this;
        getHoverView().collapse();
        if (item instanceof AppPrediction) {
            AppPrediction appPrediction = (AppPrediction) item;
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appPrediction.getPackageName());
            if (launchIntent != null) {
                context.startActivity(launchIntent);
            } else {
                Toast.makeText(context, "No launcher found for " + appPrediction.getAppName(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}