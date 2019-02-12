package com.chamika.research.smartprediction.ui.hover;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.chamika.research.smartprediction.R;
import com.chamika.research.smartprediction.prediction.AppPrediction;
import com.chamika.research.smartprediction.prediction.Prediction;
import com.chamika.research.smartprediction.ui.hover.adapters.OnItemSelectListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mattcarroll.hover.Content;
import io.mattcarroll.hover.HoverMenu;

public class MultiSectionHoverMenu extends HoverMenu {

    private static final String TAG = MultiSectionHoverMenu.class.getSimpleName();

    private final Context mContext;
    private final List<Section> mSections;
    private OnItemSelectListener<Prediction> onItemSelectListener;

    public MultiSectionHoverMenu(@NonNull Context context, List<Prediction> predictions, OnItemSelectListener<Prediction> onItemSelectListener) {
        mContext = context.getApplicationContext();
        this.onItemSelectListener = onItemSelectListener;

        mSections = new ArrayList<>();

        Map<Prediction.Type, List<Prediction>> map = new HashMap<>();
        for (Prediction prediction : predictions) {
            List<Prediction> predictionList = map.get(prediction.getType());
            if (predictionList == null) {
                predictionList = new ArrayList<>();
                map.put(prediction.getType(), predictionList);
            }
            predictionList.add(prediction);
        }

        for (Map.Entry<Prediction.Type, List<Prediction>> entry : map.entrySet()) {
            switch (entry.getKey()) {
                case APP:
                    List<AppPrediction> predictionList = (List<AppPrediction>) (List<?>) entry.getValue();
                    prepareAppPredictions(predictionList);
                    AppPrediction appPrediction = (AppPrediction) entry.getValue().get(0);

                    mSections.add(
                            new Section(
                                    new SectionId(appPrediction.getId()),
                                    createAppIconView(appPrediction),
                                    createAppScreen(mContext, predictionList)

                            )
                    );
                    break;
            }
        }
    }

    private void prepareAppPredictions(List<AppPrediction> predictionList) {
        List<AppPrediction> removeList = new ArrayList<>();
        for (AppPrediction appPrediction : predictionList) {
            String packageName = appPrediction.getPackageName();
            try {
                PackageManager packageManager = mContext.getPackageManager();
                Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
                if (launchIntent == null) {
                    removeList.add(appPrediction);
                    continue;
                }
                Drawable icon = packageManager.getApplicationIcon(packageName);
                ApplicationInfo info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                String appName = (String) packageManager.getApplicationLabel(info);
                if (appName == null || appName.isEmpty() || icon == null) {
                    removeList.add(appPrediction);
                    continue;
                }
                appPrediction.setAppName(appName);
                appPrediction.setAppIcon(icon);
            } catch (Exception e) {
                Log.e(TAG, "Unable to find app icon for :" + packageName, e);
                removeList.add(appPrediction);
            }
        }

        if (!removeList.isEmpty()) {
            predictionList.removeAll(removeList);
        }
    }

    private View createTabView() {
        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(R.drawable.tab_background);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return imageView;
    }

    private View createAppIconView(AppPrediction prediction) {
        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(R.drawable.tab_background);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageDrawable(prediction.getAppIcon());
        return imageView;
    }

    private Content createAppScreen(Context context, List<AppPrediction> appPredictions) {
        return new AppListContent(context, appPredictions, onItemSelectListener);
    }

    @Override
    public String getId() {
        return "multisectionmenu";
    }

    @Override
    public int getSectionCount() {
        return mSections.size();
    }

    @Nullable
    @Override
    public Section getSection(int index) {
        return mSections.get(index);
    }

    @Nullable
    @Override
    public Section getSection(@NonNull SectionId sectionId) {
        for (Section section : mSections) {
            if (section.getId().equals(sectionId)) {
                return section;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public List<Section> getSections() {
        return new ArrayList<>(mSections);
    }

}