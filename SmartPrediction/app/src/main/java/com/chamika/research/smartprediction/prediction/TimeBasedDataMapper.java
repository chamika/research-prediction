package com.chamika.research.smartprediction.prediction;

import android.database.Cursor;
import android.util.Log;

import com.chamika.research.smartprediction.store.BaseStore;

import net.sf.javaml.core.Dataset;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeBasedDataMapper implements ClusteringDataMapper {

    private final static String TAG = TimeBasedDataMapper.class.getSimpleName();

    private static final boolean ENABLE_APP = true;
    private static final boolean ENABLE_SMS = true;
    private static final boolean ENABLE_CALL = true;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss", Locale.US);

    public static double getTimeOfDay(Calendar cal) {
        return (cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)) / 1440.0;
    }

    public static double getDayOfWeek(Calendar cal) {
        return (cal.get(Calendar.DAY_OF_WEEK) - 1) / 6.0;
    }

    @Override
    public String map(Cursor cursor) {
        double dayOfWeek, timeOfDay, event, d1, d2, d3;

        long id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseStore.EventsStructure._ID));
        int actionType = cursor.getInt(cursor.getColumnIndexOrThrow(BaseStore.EventsStructure.COLUMN_NAME_ACTION_TYPE));
        String action = "N/A";
        if (actionType == 1) {
            action = "ACT";
        } else if (actionType == 2) {
            action = "EVENT";
        } else if (actionType == 3) {
            action = "ACT/EVT";
        }
        String eventType = cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.EventsStructure.COLUMN_NAME_EVENT_TYPE));

        long timeInMilis = cursor.getLong(cursor.getColumnIndexOrThrow(BaseStore.EventsStructure.COLUMN_NAME_TIME));
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMilis);
        String data1 = cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.EventsStructure.COLUMN_NAME_DATA1));
        String data2 = cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.EventsStructure.COLUMN_NAME_DATA2));
        String data3 = cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.EventsStructure.COLUMN_NAME_DATA3));
        String data4 = cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.EventsStructure.COLUMN_NAME_DATA4));

        dayOfWeek = getDayOfWeek(cal);
        timeOfDay = getTimeOfDay(cal);
        switch (eventType) {
            case "ACT":
                event = 1.0;
                break;
            case "CALL":
                event = 3.0;
                break;
            case "SMS":
                event = 4.0;
                break;
            case "APP":
                event = 5.0;
                break;
            default:
                event = 0.0;
        }

        //skip unknown events after testing
        if (event == 0.0) {
            return null;
        }

        if ((!ENABLE_APP && event == 5.0) || (!ENABLE_CALL && event == 3.0) || (!ENABLE_SMS && event == 4.0)) {
            return null;
        }

        d1 = data1.hashCode();
        String text = eventType + "|" + data1 + "|" + sdf.format(cal.getTime());

//                String format = String.format(Locale.US, "%.4f,%.4f,%.0f,%s", dayOfWeek, timeOfDay, event, text);
        String format = String.format(Locale.US, "%.4f,%.4f,%s", dayOfWeek, timeOfDay, text);
        Log.d(TAG, format);
        return format;
    }

    @Override
    public int getClassIndex() {
        return 2;
    }

    @Override
    public double generateKey(Dataset dataset) {
        double dayOfWeek = dataset.get(0).value(0);
        double timestep = dataset.get(0).value(1);
        return generateKey(dayOfWeek, timestep);
    }

    @Override
    public double generateKey(Event event) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(event.getDate());
        double dayOfWeek = TimeBasedDataMapper.getDayOfWeek(cal);
        double timestep = TimeBasedDataMapper.getTimeOfDay(cal);
        return generateKey(dayOfWeek, timestep);
    }

    @Override
    public Event reverseKey(String entry) {
        String[] splits = entry.split(",");
        String classText = splits[getClassIndex()];
        String[] classValues = classText.split("\\|");
        Date eventTime = null;
        try {
            eventTime = sdf.parse(classValues[2]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Event(eventTime);
    }

    private double generateKey(double dayOfWeek, double timestep) {
        return Math.round(dayOfWeek * 6.0) + timestep;
    }
}
