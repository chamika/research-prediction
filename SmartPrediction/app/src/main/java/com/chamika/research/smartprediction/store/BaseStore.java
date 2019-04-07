package com.chamika.research.smartprediction.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.Arrays;


public class BaseStore {

    private static final String TAG = BaseStore.class.getSimpleName();

    private static SQLiteDatabase dbReadable;
    private static SQLiteDatabase dbWritable;

    private BaseStore() {
    }

    public static long saveEvent(Context context, int actionType, String eventType, long time,
                                 String... data) {
        initWritableDB(context);

        ContentValues values = new ContentValues();
        values.put(EventsStructure.COLUMN_NAME_ACTION_TYPE, actionType);
        values.put(EventsStructure.COLUMN_NAME_EVENT_TYPE, eventType);
        values.put(EventsStructure.COLUMN_NAME_TIME, time);
        if (data.length > 0) {
            values.put(EventsStructure.COLUMN_NAME_DATA1, data[0]);
        }
        if (data.length > 1) {
            values.put(EventsStructure.COLUMN_NAME_DATA2, data[1]);
        }
        if (data.length > 2) {
            values.put(EventsStructure.COLUMN_NAME_DATA3, data[2]);
        }
        if (data.length > 3) {
            values.put(EventsStructure.COLUMN_NAME_DATA4, data[3]);
        }
        dbWritable.insert(EventsStructure.TABLE_NAME_EVENTS, null, values);
        Log.d(TAG, "Insert " + EventsStructure.TABLE_NAME_EVENTS + " row:" + actionType + "," + eventType + "," + Arrays.toString(data));
        return 1;
    }

    public static int savePrediction(Context context, long predictionId, int predictor, int predictionIndex, String eventType, long time, String... data) {
        initWritableDB(context);

        ContentValues values = new ContentValues();
        values.put(PredictionsStructure.COLUMN_NAME_PREDICTION_ID, predictionId);
        values.put(PredictionsStructure.COLUMN_NAME_PREDICTOR, predictor);
        values.put(PredictionsStructure.COLUMN_NAME_INDEX, predictionIndex);
        values.put(PredictionsStructure.COLUMN_NAME_TYPE, eventType);
        values.put(PredictionsStructure.COLUMN_NAME_TIME, time);
        if (data.length > 0) {
            values.put(PredictionsStructure.COLUMN_NAME_DATA1, data[0]);
        }
        if (data.length > 1) {
            values.put(PredictionsStructure.COLUMN_NAME_DATA2, data[1]);
        }
        dbWritable.insert(PredictionsStructure.TABLE_NAME, null, values);
        Log.d(TAG, "Insert " + PredictionsStructure.TABLE_NAME + " row:" + predictor + "," + eventType + "," + Arrays.toString(data));
        return 1;
    }

//    public static Cursor getAppsFromState(Context context, String tableName, String type, String state) {
//        DBHelper mDbHelper = new DBHelper(context);
//
//        if (dbReadable == null || !dbReadable.isOpen()) {
//            dbReadable = mDbHelper.getReadableDatabase();
//        }
//
//        String[] projection = {
//                EventsStructure._ID,
//                EventsStructure.COLUMN_NAME_APP,
//                EventsStructure.COLUMN_NAME_TYPE,
//                EventsStructure.COLUMN_NAME_STATE,
//                EventsStructure.COLUMN_NAME_HITS
//        };
//
//        String selection = EventsStructure.COLUMN_NAME_TYPE + " = ? and " + EventsStructure.COLUMN_NAME_STATE + " = ?";
//        String[] selectionArgs = {type, state};
//
//        String sortOrder = EventsStructure.COLUMN_NAME_HITS + " DESC";
//
//        return dbReadable.query(
//                tableName,      // The table to query
//                projection,     // The columns to return
//                selection,      // The columns for the WHERE clause
//                selectionArgs,  // The values for the WHERE clause
//                null,           // don't group the rows
//                null,           // don't filter by row groups
//                sortOrder       // The sort order
//        );
//    }
//
//    public static Cursor getAppsFromState(Context context, String tableName, String type, List<String> states) {
//        DBHelper mDbHelper = new DBHelper(context);
//
//        if (dbReadable == null || !dbReadable.isOpen()) {
//            dbReadable = mDbHelper.getReadableDatabase();
//        }
//
//        String[] projection = {
//                EventsStructure.COLUMN_NAME_APP
//        };
//
//        StringBuilder inBuilder = new StringBuilder(EventsStructure.COLUMN_NAME_STATE + " IN ( ");
//        for (int i = 0; i < states.size(); i++) {
//            if (i != 0) {
//                inBuilder.append(" , ");
//            }
//            inBuilder.append(" ? ");
//        }
//        inBuilder.append(" ) and ");
//        inBuilder.append(EventsStructure.COLUMN_NAME_TYPE);
//        inBuilder.append(" = ? ");
//        String selection = inBuilder.toString();
//
//        String[] selectionArgs = Arrays.copyOf(states.toArray(new String[0]), states.size() + 1);
//        selectionArgs[selectionArgs.length - 1] = type;
//
//        String sortOrder = EventsStructure.COLUMN_NAME_HITS + " DESC";
//
//        return dbReadable.query(
//                true,           //distinct
//                tableName,      // The table to query
//                projection,     // The columns to return
//                selection,      // The columns for the WHERE clause
//                selectionArgs,  // The values for the WHERE clause
//                null,           // don't group the rows
//                null,           // don't filter by row groups
//                sortOrder,       // The sort order
//                null           // no limit
//        );
//    }

//    public static long saveEvent(Context context, String tableName, String app, String type, String state, int hitIncrement) {
//        DBHelper mDbHelper = new DBHelper(context);
//
//        if (dbWritable == null || !dbWritable.isOpen()) {
//            dbWritable = mDbHelper.getReadableDatabase();
//        }
//
//        dbWritable.execSQL("UPDATE " + tableName + " set " + EventsStructure.COLUMN_NAME_HITS + " = " + EventsStructure.COLUMN_NAME_HITS + " + " + hitIncrement +
//                " where " + EventsStructure.COLUMN_NAME_APP + " = ? and " + EventsStructure.COLUMN_NAME_TYPE + " = ? and " + EventsStructure.COLUMN_NAME_STATE + " = ?", new String[]{app, type, state});
//
//        SQLiteStatement statement = dbWritable.compileStatement("SELECT changes()");
//        long affectedRows = statement.simpleQueryForLong();
//
//        if (affectedRows > 0) {
//            Log.d(TAG, "Updated rows:" + affectedRows + " data=" + app + "," + state + "," + hitIncrement);
//            return affectedRows;
//        } else {
//            ContentValues values = new ContentValues();
//            values.put(EventsStructure.COLUMN_NAME_APP, app);
//            values.put(EventsStructure.COLUMN_NAME_TYPE, type);
//            values.put(EventsStructure.COLUMN_NAME_STATE, state);
//            values.put(EventsStructure.COLUMN_NAME_HITS, hitIncrement);
//            dbWritable.insert(tableName, null, values);
//            Log.d(TAG, "Insert row:" + app + "," + state + "," + hitIncrement);
//            return 1;
//        }
//    }

    public static Cursor getAllEvents(Context context) {
        return getAllEvents(context, "DESC");
    }

    public static Cursor getAllEvents(Context context, String sort) {
        initReadableDB(context);

        String[] projection = {
                EventsStructure._ID,
                EventsStructure.COLUMN_NAME_ACTION_TYPE,
                EventsStructure.COLUMN_NAME_EVENT_TYPE,
                EventsStructure.COLUMN_NAME_TIME,
                EventsStructure.COLUMN_NAME_DATA1,
                EventsStructure.COLUMN_NAME_DATA2,
                EventsStructure.COLUMN_NAME_DATA3,
                EventsStructure.COLUMN_NAME_DATA4
        };

//        String selection = EventsStructure.COLUMN_NAME_TYPE + " = ? and " + EventsStructure.COLUMN_NAME_STATE + " = ?";
//        String[] selectionArgs = {type, state};

        String sortOrder = EventsStructure.COLUMN_NAME_TIME + " " + sort;

        return dbReadable.query(
                EventsStructure.TABLE_NAME_EVENTS,      // The table to query
                projection,     // The columns to return
                null,      // The columns for the WHERE clause
                null,  // The values for the WHERE clause
                null,           // don't group the rows
                null,           // don't filter by row groups
                sortOrder       // The sort order
        );
    }

    public static Cursor getEventsWithTypeAndTime(Context context, String eventType, long timeFrom, long timeTo) {
        initReadableDB(context);

        String[] projection = {
                EventsStructure._ID,
                EventsStructure.COLUMN_NAME_ACTION_TYPE,
                EventsStructure.COLUMN_NAME_EVENT_TYPE,
                EventsStructure.COLUMN_NAME_TIME,
                EventsStructure.COLUMN_NAME_DATA1,
                EventsStructure.COLUMN_NAME_DATA2,
                EventsStructure.COLUMN_NAME_DATA3,
                EventsStructure.COLUMN_NAME_DATA4
        };

        String selection = EventsStructure.COLUMN_NAME_TIME + " >= ? and " + EventsStructure.COLUMN_NAME_TIME + " <= ? and "
                + EventsStructure.COLUMN_NAME_EVENT_TYPE + " = ? ";
        String[] selectionArgs = {String.valueOf(timeFrom), String.valueOf(timeTo), eventType};

        String sortOrder = EventsStructure.COLUMN_NAME_TIME + " DESC";

        return dbReadable.query(
                EventsStructure.TABLE_NAME_EVENTS,      // The table to query
                projection,     // The columns to return
                selection,      // The columns for the WHERE clause
                selectionArgs,  // The values for the WHERE clause
                null,           // don't group the rows
                null,           // don't filter by row groups
                sortOrder       // The sort order
        );
    }


    public static synchronized void closeDBs() {
        if (dbReadable != null && dbReadable.isOpen()) {
            dbReadable.close();
            dbReadable = null;
        }
        if (dbWritable != null && dbWritable.isOpen()) {
            dbWritable.close();
            dbWritable.close();
        }
    }

    private static synchronized void initReadableDB(Context context) {
        if (dbReadable == null || !dbReadable.isOpen()) {
            DBHelper mDbHelper = new DBHelper(context);
            dbReadable = mDbHelper.getReadableDatabase();
        }
    }

    private static synchronized void initWritableDB(Context context) {
        if (dbWritable == null || !dbWritable.isOpen()) {
            DBHelper mDbHelper = new DBHelper(context);
            dbWritable = mDbHelper.getWritableDatabase();
        }
    }

    public static class EventsStructure implements BaseColumns {
        public static final String TABLE_NAME_EVENTS = "events";
        public static final String COLUMN_NAME_ACTION_TYPE = "action_type";//1=action, 2=event, 3=action+event
        public static final String COLUMN_NAME_EVENT_TYPE = "event_type";//SMS, CALL
        public static final String COLUMN_NAME_TIME = "event_time";
        public static final String COLUMN_NAME_DATA1 = "data1";
        public static final String COLUMN_NAME_DATA2 = "data2";
        public static final String COLUMN_NAME_DATA3 = "data3";
        public static final String COLUMN_NAME_DATA4 = "data4";
    }

    public static class PredictionsStructure implements BaseColumns {
        public static final String TABLE_NAME = "predictions";
        public static final String COLUMN_NAME_PREDICTION_ID = "prediction_id";
        public static final String COLUMN_NAME_PREDICTOR = "predictor";
        public static final String COLUMN_NAME_INDEX = "prediction_index";// order of a specific prediction_id
        public static final String COLUMN_NAME_TYPE = "event_type";//SMS, CALL
        public static final String COLUMN_NAME_TIME = "event_time";
        public static final String COLUMN_NAME_DATA1 = "data1";
        public static final String COLUMN_NAME_DATA2 = "data2";
    }


}
