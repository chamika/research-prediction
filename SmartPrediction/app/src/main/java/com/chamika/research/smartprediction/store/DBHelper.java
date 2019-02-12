package com.chamika.research.smartprediction.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chamika on 9/11/16.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "events.db";

    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String TEXT_TYPE = " TEXT";

    //events
    private static final String SQL_CREATE_EVENTS =
            "CREATE TABLE " + BaseStore.Structure.TABLE_NAME_EVENTS + " (" +
                    BaseStore.Structure._ID + " " + INTEGER_TYPE + " PRIMARY KEY," +
                    BaseStore.Structure.COLUMN_NAME_ACTION_TYPE + INTEGER_TYPE + COMMA_SEP +
                    BaseStore.Structure.COLUMN_NAME_EVENT_TYPE + TEXT_TYPE + COMMA_SEP +
                    BaseStore.Structure.COLUMN_NAME_TIME + INTEGER_TYPE + COMMA_SEP +
                    BaseStore.Structure.COLUMN_NAME_DATA1 + TEXT_TYPE + COMMA_SEP +
                    BaseStore.Structure.COLUMN_NAME_DATA2 + TEXT_TYPE + COMMA_SEP +
                    BaseStore.Structure.COLUMN_NAME_DATA3 + TEXT_TYPE + COMMA_SEP +
                    BaseStore.Structure.COLUMN_NAME_DATA4 + TEXT_TYPE + " )";

    private static final String SQL_DELETE_EVENTS =
            "DROP TABLE IF EXISTS " + BaseStore.Structure.TABLE_NAME_EVENTS;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_EVENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_EVENTS);
        onCreate(db);
    }
}
