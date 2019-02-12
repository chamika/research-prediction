package com.chamika.research.smartprediction.ui.dataview;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.chamika.research.smartprediction.R;
import com.chamika.research.smartprediction.store.BaseStore;
import com.chamika.research.smartprediction.store.FileExport;
import com.chamika.research.smartprediction.util.Config;

import java.lang.ref.WeakReference;

public class DataViewActivity extends AppCompatActivity {
    private static final String TAG = DataViewActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_view);

        // Find ListView to populate
        ListView listView = (ListView) findViewById(R.id.listview);
        DataCursorAdapter dataCursorAdapter = new DataCursorAdapter(this, BaseStore.getDataDesc(this));
        listView.setAdapter(dataCursorAdapter);

        new DataExportTask(this).execute();
    }

    static class DataExportTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<Activity> activityReference;

        public DataExportTask(Activity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Activity context = activityReference.get();
            if (context == null) {
                return null;
            }
            FileExport.exportDBtoFile(context, Config.DATA_FILE_NAME);
            return null;
        }


    }
}
