package com.chamika.research.smartprediction;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.multidex.MultiDexApplication;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;

/**
 * Created by chamika on 3/17/17.
 */

public class MainApplication extends MultiDexApplication {
    public static int checkAppSignature(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                final String currentSignature = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.d("REMOVE_ME", "Include this string as a value for SIGNATURE:" + currentSignature);
                //compare signatures
                if ("K+stKiLWd4muJWWP7O4xSdnAd0Y=".equals(currentSignature.trim())) {
                    return 1;
                }
            }
        } catch (Exception e) {
            //assumes an issue in checking signature., but we let the caller decide on what to do.

        }

        return 0;

    }

    @Override
    public void onCreate() {
        super.onCreate();
//        ProvidersStetho providersStetho = new ProvidersStetho(this);
//        providersStetho.enableDefaults();
//
//        Stetho.initialize(Stetho.newInitializerBuilder(this)
//                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
//                .enableWebKitInspector(providersStetho.defaultInspectorModulesProvider())
//                .build());
//
//        int signature = checkAppSignature(this);
//        if (signature != 1) {
//            throw new RuntimeException("Tampered App");
//        }
    }
}
