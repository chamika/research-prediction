package com.chamika.research.smartprediction.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chamika on 9/11/16.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "events.db";

    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String TEXT_TYPE = " TEXT";

    //events
    private static final String SQL_CREATE_EVENTS =
            "CREATE TABLE " + BaseStore.EventsStructure.TABLE_NAME_EVENTS + " (" +
                    BaseStore.EventsStructure._ID + " " + INTEGER_TYPE + " PRIMARY KEY," +
                    BaseStore.EventsStructure.COLUMN_NAME_ACTION_TYPE + INTEGER_TYPE + COMMA_SEP +
                    BaseStore.EventsStructure.COLUMN_NAME_EVENT_TYPE + TEXT_TYPE + COMMA_SEP +
                    BaseStore.EventsStructure.COLUMN_NAME_TIME + INTEGER_TYPE + COMMA_SEP +
                    BaseStore.EventsStructure.COLUMN_NAME_DATA1 + TEXT_TYPE + COMMA_SEP +
                    BaseStore.EventsStructure.COLUMN_NAME_DATA2 + TEXT_TYPE + COMMA_SEP +
                    BaseStore.EventsStructure.COLUMN_NAME_DATA3 + TEXT_TYPE + COMMA_SEP +
                    BaseStore.EventsStructure.COLUMN_NAME_DATA4 + TEXT_TYPE + " )";

    //predictions
    private static final String SQL_CREATE_PREDICTIONS =
            "CREATE TABLE " + BaseStore.PredictionsStructure.TABLE_NAME + " (" +
                    BaseStore.PredictionsStructure._ID + " " + INTEGER_TYPE + " PRIMARY KEY," +
                    BaseStore.PredictionsStructure.COLUMN_NAME_PREDICTOR + INTEGER_TYPE + COMMA_SEP +
                    BaseStore.PredictionsStructure.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                    BaseStore.PredictionsStructure.COLUMN_NAME_TIME + INTEGER_TYPE + COMMA_SEP +
                    BaseStore.PredictionsStructure.COLUMN_NAME_DATA1 + TEXT_TYPE + COMMA_SEP +
                    BaseStore.PredictionsStructure.COLUMN_NAME_DATA2 + TEXT_TYPE + " )";

    private static final String SQL_DROP_PREDICTIONS =
            "DROP TABLE IF EXISTS " + BaseStore.PredictionsStructure.TABLE_NAME;

    private static final String SQL_CREATE_PREDICTIONS_V3 =
            "CREATE TABLE " + BaseStore.PredictionsStructure.TABLE_NAME + " (" +
                    BaseStore.PredictionsStructure._ID + " " + INTEGER_TYPE + " PRIMARY KEY," +
                    BaseStore.PredictionsStructure.COLUMN_NAME_PREDICTION_ID + INTEGER_TYPE + COMMA_SEP +
                    BaseStore.PredictionsStructure.COLUMN_NAME_PREDICTOR + INTEGER_TYPE + COMMA_SEP +
                    BaseStore.PredictionsStructure.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                    BaseStore.PredictionsStructure.COLUMN_NAME_TIME + INTEGER_TYPE + COMMA_SEP +
                    BaseStore.PredictionsStructure.COLUMN_NAME_DATA1 + TEXT_TYPE + COMMA_SEP +
                    BaseStore.PredictionsStructure.COLUMN_NAME_DATA2 + TEXT_TYPE + " )";

    private static final String SQL_CREATE_PREDICTIONS_V4 =
            "CREATE TABLE " + BaseStore.PredictionsStructure.TABLE_NAME + " (" +
                    BaseStore.PredictionsStructure._ID + " " + INTEGER_TYPE + " PRIMARY KEY," +
                    BaseStore.PredictionsStructure.COLUMN_NAME_PREDICTION_ID + INTEGER_TYPE + COMMA_SEP +
                    BaseStore.PredictionsStructure.COLUMN_NAME_PREDICTOR + INTEGER_TYPE + COMMA_SEP +
                    BaseStore.PredictionsStructure.COLUMN_NAME_INDEX + INTEGER_TYPE + COMMA_SEP +
                    BaseStore.PredictionsStructure.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                    BaseStore.PredictionsStructure.COLUMN_NAME_TIME + INTEGER_TYPE + COMMA_SEP +
                    BaseStore.PredictionsStructure.COLUMN_NAME_DATA1 + TEXT_TYPE + COMMA_SEP +
                    BaseStore.PredictionsStructure.COLUMN_NAME_DATA2 + TEXT_TYPE + " )";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_EVENTS);
        db.execSQL(SQL_CREATE_PREDICTIONS_V4);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 1) {
            db.execSQL(SQL_CREATE_PREDICTIONS);
        }
        if (oldVersion <= 2) {
            db.execSQL(SQL_DROP_PREDICTIONS);
            db.execSQL(SQL_CREATE_PREDICTIONS_V3);
        }
        if (oldVersion <= 3) {
            db.execSQL(SQL_DROP_PREDICTIONS);
            db.execSQL(SQL_CREATE_PREDICTIONS_V4);
        }
    }
}
