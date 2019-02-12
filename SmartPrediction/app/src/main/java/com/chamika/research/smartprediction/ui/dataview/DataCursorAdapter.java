package com.chamika.research.smartprediction.ui.dataview;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chamika.research.smartprediction.R;
import com.chamika.research.smartprediction.store.BaseStore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by chamika on 3/17/17.
 */

public class DataCursorAdapter extends CursorAdapter {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss", Locale.US);

    public DataCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.data_view_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView textActionType = (TextView) view.findViewById(R.id.text_action_type);
        TextView textEventType = (TextView) view.findViewById(R.id.text_event_type);
        TextView textTime = (TextView) view.findViewById(R.id.text_time);
        TextView textData = (TextView) view.findViewById(R.id.text_data);

        int actionType = cursor.getInt(cursor.getColumnIndexOrThrow(BaseStore.Structure.COLUMN_NAME_ACTION_TYPE));
        String action = "N/A";
        if (actionType == 1) {
            action = "ACT";
        } else if (actionType == 2) {
            action = "EVENT";
        } else if (actionType == 3) {
            action = "ACT/EVT";
        }
        textActionType.setText(action);
        textEventType.setText(cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.Structure.COLUMN_NAME_EVENT_TYPE)));
        long timeInMilis = cursor.getLong(cursor.getColumnIndexOrThrow(BaseStore.Structure.COLUMN_NAME_TIME));
        Date date = new Date(timeInMilis);
        textTime.setText((sdf.format(date)));
        String data1 = cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.Structure.COLUMN_NAME_DATA1));
        String data2 = cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.Structure.COLUMN_NAME_DATA2));
        String data3 = cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.Structure.COLUMN_NAME_DATA3));
        String data4 = cursor.getString(cursor.getColumnIndexOrThrow(BaseStore.Structure.COLUMN_NAME_DATA4));

        textData.setText(String.format("%s %s %s %s", data1, data2, data3, data4));
    }
}
