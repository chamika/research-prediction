package com.chamika.research.smartprediction.prediction;

import android.graphics.drawable.Drawable;

public class AppPrediction extends Prediction {

    private Drawable appIcon;
    private String appName;

    public AppPrediction(String id, String packageName) {
        super(id, Type.APP);
        this.data = packageName;
    }

    public String getPackageName() {
        return data;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
