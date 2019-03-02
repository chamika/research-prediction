package com.chamika.research.smartprediction;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.chamika.research.smartprediction.prediction.Event;
import com.chamika.research.smartprediction.service.BackgroundService;
import com.chamika.research.smartprediction.service.DataCollectorService;
import com.chamika.research.smartprediction.service.DataUploaderService;
import com.chamika.research.smartprediction.service.PredictionHoverMenuService;
import com.chamika.research.smartprediction.ui.dataview.DataViewActivity;
import com.chamika.research.smartprediction.ui.results.ResultsActivity;
import com.chamika.research.smartprediction.util.Config;
import com.chamika.research.smartprediction.util.Constant;
import com.chamika.research.smartprediction.util.SettingsUtil;

import java.util.Date;

import io.mattcarroll.hover.overlay.OverlayPermission;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final int REQUEST_CODE_HOVER_PERMISSION = 2000;

    private static final int PERMISSIONS_REQUEST_READ_CALL_LOG = 1000;
    private static final int PERMISSIONS_REQUEST_READ_SMS = 1001;
    private static final int PERMISSIONS_REQUEST_ACTIVITY = 1002;
    private static final int PERMISSIONS_REQUEST_LOCATION = 1003;
    private PendingIntent pendingIntent;


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Context context = this.getContext();
        Intent alarmIntent = new Intent(context.getApplicationContext(), DataCollectorService.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

        initCheckBoxDangerousPermission(rootView, R.id.check_calls, android.Manifest.permission.READ_CALL_LOG, PERMISSIONS_REQUEST_READ_CALL_LOG, Constant.PREF_CALL);
        initCheckBoxDangerousPermission(rootView, R.id.check_msgs, android.Manifest.permission.READ_SMS, PERMISSIONS_REQUEST_READ_SMS, Constant.PREF_MSG);
        initCheckBoxDangerousPermission(rootView, R.id.check_location, Manifest.permission.ACCESS_FINE_LOCATION, PERMISSIONS_REQUEST_LOCATION, Constant.PREF_LOCATION);
        initCheckBoxNormalPermission(rootView, R.id.check_activities, Constant.PREF_ACTIVITY);
        initSystemUsagePermission(rootView, R.id.check_apps, Constant.PREF_APP_USAGE);

        rootView.findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start(v);
            }
        });

        rootView.findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel(v);
            }
        });

        rootView.findViewById(R.id.btn_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view(v);
            }
        });

        rootView.findViewById(R.id.btn_cluster).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cluster(v);
            }
        });

        rootView.findViewById(R.id.btn_prediction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPrediction(v);
            }
        });

        rootView.findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEvent(new Event(new Date()));
            }
        });

        return rootView;
    }

    private void scheduleDatabaseUpload(Context context) {
        Intent alarmIntent = new Intent(context, DataUploaderService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int interval = Config.DATA_UPLOAD_INTERVAL;
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, interval, pendingIntent);
    }

    private void initCheckBoxDangerousPermission(View rootView, int checkboxResId, final String permission, final int permissionRequest, final String settingsPrefKey) {
        final Context context = this.getContext();
        CheckBox checkBox = (CheckBox) rootView.findViewById(checkboxResId);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivityFragment.this.getActivity(),
                                new String[]{permission},
                                permissionRequest);
                    } else {
                        SettingsUtil.setBooleanPref(context, settingsPrefKey, true);
                    }
                } else {
                    SettingsUtil.setBooleanPref(context, settingsPrefKey, false);
                }
            }
        });
        checkBox.setChecked(SettingsUtil.getBooleanPref(context, settingsPrefKey));
    }

    private void initCheckBoxNormalPermission(View rootView, int checkboxResId, final String settingsPrefKey) {
        final Context context = this.getContext();
        CheckBox checkBox = (CheckBox) rootView.findViewById(checkboxResId);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsUtil.setBooleanPref(context, settingsPrefKey, isChecked);
            }
        });
        checkBox.setChecked(SettingsUtil.getBooleanPref(context, settingsPrefKey));
    }

    private void initSystemUsagePermission(View rootView, int checkboxResId, final String settingsPrefKey) {
        final Context context = this.getContext();
        final CheckBox checkBox = (CheckBox) rootView.findViewById(checkboxResId);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    boolean granted = false;
                    AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                    int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                            android.os.Process.myUid(), context.getPackageName());

                    if (mode == AppOpsManager.MODE_DEFAULT) {
                        granted = (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
                    } else {
                        granted = (mode == AppOpsManager.MODE_ALLOWED);
                    }
                    if (!granted) {
                        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                        checkBox.setChecked(false);
                    } else {
                        SettingsUtil.setBooleanPref(context, settingsPrefKey, true);
                    }
                } else {
                    SettingsUtil.setBooleanPref(context, settingsPrefKey, false);
                }
            }
        });
        checkBox.setChecked(SettingsUtil.getBooleanPref(context, settingsPrefKey));
    }

    public void start(View v) {
        //SMS,CALL
        Context context = this.getContext();
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int interval = Config.DATA_COLLECTION_REFRESH_INTERVAL;
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);

        //ACTIVITY, LOCATION
        this.getActivity().startService(new Intent(context.getApplicationContext(), BackgroundService.class));

        scheduleDatabaseUpload(context);
        Toast.makeText(context, "Started collecting data", Toast.LENGTH_SHORT).show();
    }

    public void cancel(View v) {
        Context context = this.getContext();
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        this.getActivity().stopService(new Intent(context.getApplicationContext(), BackgroundService.class));

        Toast.makeText(context, "Stopped collecting data", Toast.LENGTH_SHORT).show();
    }

    public void view(View v) {
        this.getActivity().startActivity(new Intent(this.getContext(), DataViewActivity.class));
    }


    private void cluster(View v) {
        this.getActivity().startActivity(new Intent(this.getContext(), ResultsActivity.class));
    }

    private void startPrediction(View v) {
        //check for permission
        Context context = this.getContext();
        if (OverlayPermission.hasRuntimePermissionToDrawOverlay(context)) {
            this.getActivity().startService(new Intent(context.getApplicationContext(), PredictionHoverMenuService.class));
        } else {
            Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + context.getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_HOVER_PERMISSION);
        }
    }

    private void sendEvent(Event event) {
        Intent i = new Intent(this.getContext(), PredictionHoverMenuService.class);
        i.putExtra(PredictionHoverMenuService.INTENT_EXTRA_SCREEN_EVENT, event);
        this.getContext().startService(i);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        String setting = null;
        int resId = 0;
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CALL_LOG: {
                setting = Constant.PREF_CALL;
                resId = R.id.check_calls;
                break;
            }
            case PERMISSIONS_REQUEST_READ_SMS: {
                setting = Constant.PREF_MSG;
                resId = R.id.check_msgs;
                break;
            }
            case PERMISSIONS_REQUEST_LOCATION: {
                setting = Constant.PREF_LOCATION;
                resId = R.id.check_location;
                break;
            }
        }
        if (setting != null) {
            ((CheckBox) getView().findViewById(resId)).setChecked(granted);
            SettingsUtil.setBooleanPref(this.getContext(), setting, granted);
        }
    }
}
