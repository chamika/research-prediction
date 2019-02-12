package com.chamika.research.smartprediction.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by chamika on 3/20/17.
 */

public class DataUploaderService extends BroadcastReceiver {

    public static final String TAG = DataUploaderService.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        uploadDatabase(context);
    }

    private void uploadDatabase(Context context) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String filename = new SimpleDateFormat("yyyyMMddHH:mm:ss.SSS", Locale.US).format(new Date()) + ".db";

        StorageReference fileRef = storageRef.child("userData").child(id).child(filename);
        try {
            File currentDB = context.getDatabasePath("events.db");
            if (currentDB.exists()) {
                fileRef.putFile(Uri.fromFile(currentDB));
                Log.d(TAG, "Database file uploaded. file name:" + filename);
            } else {
                Log.d(TAG, "Database file not found." + filename);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
