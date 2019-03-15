package com.chamika.research.smartprediction.store;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FileExport {

    private static final boolean ENABLE_APP = true;
    private static final boolean ENABLE_SMS = true;
    private static final boolean ENABLE_CALL = true;

    private static final String TAG = FileExport.class.getSimpleName();

    public static void exportDBtoFile(Context context, String fileName) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss", Locale.US);
        Cursor cursor = BaseStore.getDataDesc(context);
        cursor.moveToFirst();

        FileOutputStream fos = null;
        BufferedWriter bw = null;
        try {
            File fout = new File(context.getFilesDir(), fileName);

            fos = new FileOutputStream(fout);
            bw = new BufferedWriter(new OutputStreamWriter(fos));

            while (cursor.moveToNext()) {
                double dayOfWeek, timeOfDay, event, d1, d2, d3;

                long id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseStore.Structure._ID));
                int actionType = cursor.getInt(cursor.getColumnIndexOrThrow(BaseStore.Structure.COLUMN_NAME_ACTION_TYPE));
                String action = "N/A";
                if (actionType == 1) {
                    action = "ACT";
                } else if (actionType == 2) {
                    action = "EVENT";
                } else if (actionType == 3) {
                    action = "ACT/EVT";
                }
                String eventType = cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.Structure.COLUMN_NAME_EVENT_TYPE));

                long timeInMilis = cursor.getLong(cursor.getColumnIndexOrThrow(BaseStore.Structure.COLUMN_NAME_TIME));
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(timeInMilis);
                String data1 = cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.Structure.COLUMN_NAME_DATA1));
                String data2 = cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.Structure.COLUMN_NAME_DATA2));
                String data3 = cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.Structure.COLUMN_NAME_DATA3));
                String data4 = cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.Structure.COLUMN_NAME_DATA4));

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
                    continue;
                }

                if ((!ENABLE_APP && event == 5.0) || (!ENABLE_CALL && event == 3.0) || (!ENABLE_SMS && event == 4.0)) {
                    continue;
                }

                d1 = data1.hashCode();
                String text = eventType + "|" + data1 + "|" + sdf.format(cal.getTime());

//                String format = String.format(Locale.US, "%.4f,%.4f,%.0f,%s", dayOfWeek, timeOfDay, event, text);
                String format = String.format(Locale.US, "%.4f,%.4f,%s", dayOfWeek, timeOfDay, text);
                Log.d(TAG, format);
                bw.write(format);
                bw.newLine();
            }

            Log.d(TAG, "File export complete. Output file: " + fout.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static double getTimeOfDay(Calendar cal) {
        return (cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)) / 1440.0;
    }

    public static double getDayOfWeek(Calendar cal) {
        return (cal.get(Calendar.DAY_OF_WEEK) - 1) / 6.0;
    }
}
