package com.chamika.research.smartprediction.store;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.chamika.research.smartprediction.prediction.DataMapper;
import com.chamika.research.smartprediction.util.EventType;
import com.chamika.research.smartprediction.util.StringUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileExport {

    private static final String TAG = FileExport.class.getSimpleName();

    public static void exportDBtoFile(Context context, String fileName, DataMapper dataMapper) {
        Cursor cursor = BaseStore.getAllEvents(context, "ASC");
        cursor.moveToFirst();

        FileOutputStream fos = null;
        BufferedWriter bw = null;
        try {
            File fout = new File(context.getFilesDir(), fileName);

            fos = new FileOutputStream(fout);
            bw = new BufferedWriter(new OutputStreamWriter(fos));

            if (cursor.moveToNext()) {
                while (cursor.moveToNext()) {
                    String format = dataMapper.map(cursor);
                    if (format != null && !format.isEmpty()) {
                        format = format.replace("\n", "");
                        format = format.replace("\r", "");
                        bw.write(format);
                        bw.newLine();
                    }
                }
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

    public static void fixEncryptionMismatch(Context context) {
        Cursor cursor = BaseStore.getAllEvents(context);
        while (cursor.moveToNext()) {
            long id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseStore.EventsStructure._ID));
            String eventType = cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.EventsStructure.COLUMN_NAME_EVENT_TYPE));
            String data1 = cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.EventsStructure.COLUMN_NAME_DATA1));
            if (EventType.SMS.text().equals(eventType) || EventType.CALL.text().equals(eventType)) {
                int rows = BaseStore.updateEventData1(context, id, StringUtil.encrypt(context, StringUtil.decrypt(context, data1)));
                if (rows != 1) {
                    throw new RuntimeException("Updated more than 1 rows: _id" + id);
                }
            }
        }
    }
}
