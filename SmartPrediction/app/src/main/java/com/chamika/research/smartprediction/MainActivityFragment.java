package com.chamika.research.smartprediction;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.chamika.research.smartprediction.prediction.Event;
import com.chamika.research.smartprediction.service.DataUploaderService;
import com.chamika.research.smartprediction.service.PredictionService;
import com.chamika.research.smartprediction.service.ScheduleDataCollectorService;
import com.chamika.research.smartprediction.service.UserActivityCollectorService;
import com.chamika.research.smartprediction.ui.dataview.DataViewActivity;
import com.chamika.research.smartprediction.ui.results.ResultsActivity;
import com.chamika.research.smartprediction.util.Config;
import com.chamika.research.smartprediction.util.Constant;
import com.chamika.research.smartprediction.util.SettingsUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private boolean serviceRunning = false;
    private Button btnStart;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_new, container, false);

        Context context = this.getContext();
        Intent alarmIntent = new Intent(context.getApplicationContext(), ScheduleDataCollectorService.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

        initCheckBoxDangerousPermission(rootView, R.id.check_calls, new String[]{android.Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CALL_LOG, Constant.PREF_CALL);
        initCheckBoxDangerousPermission(rootView, R.id.check_msgs, new String[]{android.Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_SMS, Constant.PREF_MSG);
        initCheckBoxDangerousPermission(rootView, R.id.check_location, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION, Constant.PREF_LOCATION);
        initCheckBoxNormalPermission(rootView, R.id.check_activities, Constant.PREF_ACTIVITY);
        initSystemUsagePermission(rootView, R.id.check_apps, Constant.PREF_APP_USAGE);

//        rootView.findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                start(v);
//            }
//        });

//        rootView.findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                cancel(v);
//            }
//        });

//        rootView.findViewById(R.id.btn_view).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                view(v);
//            }
//        });

//        rootView.findViewById(R.id.btn_cluster).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                cluster(v);
//            }
//        });

        btnStart = rootView.findViewById(R.id.btn_prediction);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPrediction(v);
            }
        });

//        rootView.findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendEvent(new Event(new Date()));
//            }
//        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_suggest) {
            sendEvent(new Event(new Date()));
        } else if (id == R.id.action_cluster) {
            cluster();
        } else if (id == R.id.action_view) {
            view();
        }
        return super.onOptionsItemSelected(item);
    }

    private void scheduleDatabaseUpload(Context context) {
        Intent alarmIntent = new Intent(context, DataUploaderService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int interval = Config.DATA_UPLOAD_INTERVAL;
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, interval, pendingIntent);
    }

    private void initCheckBoxDangerousPermission(View rootView, int checkboxResId, final String[] permissions, final int permissionRequest, final String settingsPrefKey) {
        final Context context = this.getContext();
        Switch checkBox = rootView.findViewById(checkboxResId);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    List<String> deniedPermissions = new ArrayList<>();
                    for (String permission : permissions) {
                        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                            deniedPermissions.add(permission);
                        }
                    }
                    if (!deniedPermissions.isEmpty()) {
                        ActivityCompat.requestPermissions(MainActivityFragment.this.getActivity(),
                                deniedPermissions.toArray(new String[0]),
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
        Switch checkBox = rootView.findViewById(checkboxResId);
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
        final Switch checkBox = rootView.findViewById(checkboxResId);
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

    public void start() {
        //SMS,CALL
        Context context = this.getContext();
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int interval = Config.DATA_COLLECTION_REFRESH_INTERVAL;
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);

        //ACTIVITY, LOCATION
        this.getActivity().startService(new Intent(context.getApplicationContext(), UserActivityCollectorService.class));

        scheduleDatabaseUpload(context);
        Toast.makeText(context, "Started collecting data", Toast.LENGTH_SHORT).show();
    }

    public void cancel() {
        Context context = this.getContext();
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        this.getActivity().stopService(new Intent(context.getApplicationContext(), UserActivityCollectorService.class));

        Toast.makeText(context, "Stopped collecting data", Toast.LENGTH_SHORT).show();
    }

    public void view() {
        this.getActivity().startActivity(new Intent(this.getContext(), DataViewActivity.class));
    }


    private void cluster() {
        this.getActivity().startActivity(new Intent(this.getContext(), ResultsActivity.class));
    }

    private void startPrediction(View v) {
        //check for permission
        Context context = this.getContext();
        if (context != null) {
            if (OverlayPermission.hasRuntimePermissionToDrawOverlay(context)) {
                Intent intent = new Intent(context.getApplicationContext(), PredictionService.class);
                if (serviceRunning) {
                    intent.putExtra(PredictionService.INTENT_EXTRA_STOP, true);
                }
                serviceRunning = !serviceRunning;
                startHoverService(intent);
                updateUI();
            } else {
                Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + context.getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_HOVER_PERMISSION);
            }
        }
    }

    private void updateUI() {
        if (serviceRunning) {
            btnStart.setText(R.string.button_stop);
        } else {
            btnStart.setText(R.string.button_start);
        }
    }

    private void sendEvent(Event event) {
        Intent i = new Intent(this.getContext(), PredictionService.class);
        i.putExtra(PredictionService.INTENT_EXTRA_SCREEN_EVENT, event);
        startHoverService(i);
    }

    private void startHoverService(Intent intent) {
        Context context = this.getContext();
        if (context != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
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
            ((Switch) getView().findViewById(resId)).setChecked(granted);
            SettingsUtil.setBooleanPref(this.getContext(), setting, granted);
        }
    }
}
