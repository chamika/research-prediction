package com.chamika.research.smartprediction.ui.hover;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.chamika.research.smartprediction.R;
import com.chamika.research.smartprediction.prediction.AppPrediction;
import com.chamika.research.smartprediction.prediction.CallPrediction;
import com.chamika.research.smartprediction.prediction.ContactPrediction;
import com.chamika.research.smartprediction.prediction.MessagePrediction;
import com.chamika.research.smartprediction.prediction.Prediction;
import com.chamika.research.smartprediction.ui.hover.adapters.OnItemSelectListener;
import com.chamika.research.smartprediction.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.mattcarroll.hover.Content;
import io.mattcarroll.hover.HoverMenu;
import me.everything.providers.android.contacts.Contact;
import me.everything.providers.android.contacts.ContactsProvider;
import me.everything.providers.core.Data;

public class MultiSectionHoverMenu extends HoverMenu {

    private static final String TAG = MultiSectionHoverMenu.class.getSimpleName();

    private final Context mContext;
    private final List<Section> mSections = new ArrayList<>();
    private OnItemSelectListener<Prediction> onItemSelectListener;

    public MultiSectionHoverMenu(@NonNull Context context, OnItemSelectListener<Prediction> onItemSelectListener) {
        mContext = context.getApplicationContext();
        this.onItemSelectListener = onItemSelectListener;
    }

    public void updateSections(List<Prediction> predictions) {
        mSections.clear();

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
                case APP: {
                    List<AppPrediction> predictionList = (List<AppPrediction>) (List<?>) entry.getValue();
                    prepareAppPredictions(predictionList);
                    if (!predictionList.isEmpty()) {
                        AppPrediction appPrediction = (AppPrediction) entry.getValue().get(0);

                        mSections.add(
                                new Section(
                                        new SectionId(appPrediction.getPackageName()),
                                        createAppIconView(appPrediction),
                                        createAppScreen(mContext, predictionList)

                                )
                        );
                        break;
                    }
                }

                case SMS: {
                    List<MessagePrediction> predictionList = (List<MessagePrediction>) (List<?>) entry.getValue();
                    prepareContactPredictions(predictionList);
                    if (!predictionList.isEmpty()) {
                        mSections.add(
                                new Section(
                                        new SectionId(Prediction.Type.SMS.name()),
                                        createMessageIcon(),
                                        createContactScreen(mContext, predictionList)

                                )
                        );
                    }
                    break;
                }

                case CALL: {
                    List<CallPrediction> predictionList = (List<CallPrediction>) (List<?>) entry.getValue();
                    prepareContactPredictions(predictionList);
                    if (!predictionList.isEmpty()) {
                        mSections.add(
                                new Section(
                                        new SectionId(Prediction.Type.CALL.name()),
                                        createCallIcon(),
                                        createContactScreen(mContext, predictionList)

                                )
                        );
                    }
                    break;
                }
            }
        }
    }

    private void prepareContactPredictions(List<? extends ContactPrediction> predictionList) {
        Map<String, com.chamika.research.smartprediction.ui.hover.Contact> numbersMap = new HashMap<>();

        ContactsProvider provider = new ContactsProvider(mContext);
        Data<Contact> contacts = provider.getContacts();
        if (contacts != null) {
            List<Contact> list = contacts.getList();
            if (list != null && !list.isEmpty()) {
                for (Contact contact : list) {
                    numbersMap.put(contact.phone,
                            new com.chamika.research.smartprediction.ui.hover.Contact(contact.displayName, contact.phone, contact.uriPhoto));
                }
            }
        }

        if (predictionList != null) {
            Iterator<? extends ContactPrediction> iterator = predictionList.iterator();
            while (iterator.hasNext()) {
                ContactPrediction contactPrediction = iterator.next();
                String realNumber = StringUtil.decrypt(mContext, contactPrediction.getEncryptedNumber());
                com.chamika.research.smartprediction.ui.hover.Contact contact = numbersMap.get(realNumber);
                if (contact != null) {
                    contactPrediction.setName(contact.getName());
                    contactPrediction.setNumber(contact.getNumber());
                    contactPrediction.setUri(contact.getUri());
                } else {
                    contactPrediction.setName(realNumber);
                    contactPrediction.setNumber(realNumber);
                    contactPrediction.setUri(null);
                    //remove if needed
//                    iterator.remove();
                }
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
//        imageView.setImageResource(R.drawable.tab_background);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return imageView;
    }

    private View createAppIconView(AppPrediction prediction) {
        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageDrawable(prediction.getAppIcon());
        return imageView;
    }

    private View createMessageIcon() {
        View parent = LayoutInflater.from(mContext).inflate(R.layout.hover_icon, null, false);
        ImageView imageView = parent.findViewById(R.id.icon);
        imageView.setImageResource(R.drawable.ic_sms_prediction);
        return parent;
    }

    private View createCallIcon() {
        View parent = LayoutInflater.from(mContext).inflate(R.layout.hover_icon, null, false);
        ImageView imageView = parent.findViewById(R.id.icon);
        imageView.setImageResource(R.drawable.ic_call_prediction);
        return parent;
    }


    private Content createAppScreen(Context context, List<AppPrediction> predictions) {
        return new AppListContent(context, predictions, onItemSelectListener);
    }

    private Content createContactScreen(Context context, List<? extends ContactPrediction> predictions) {
        return new ContactListContent(context, predictions, onItemSelectListener);
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