package com.chamika.research.smartprediction.ui.results;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chamika.research.smartprediction.R;
import com.chamika.research.smartprediction.util.Clustering;
import com.chamika.research.smartprediction.util.Config;

import net.sf.javaml.core.Dataset;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ResultsActivity extends AppCompatActivity {

    public static final String TAG = ResultsActivity.class.getSimpleName();
    private EditText edtFilepath;
    private EditText edtClusterAmount;
    private TextView txtClusterDetail;
    private SeekBar seekTime;
    private TextView txtSelectedTime;
    private RecyclerView recClusters;
    private ProgressBar progressBar;

    private NavigableMap<Integer, Dataset> clusteredData = new TreeMap<>();
    private LinearLayoutManager layoutManager;
    private RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        init();
    }

    private void init() {
        edtFilepath = (EditText) findViewById(R.id.edt_file_path);
        edtClusterAmount = (EditText) findViewById(R.id.edt_cluster_amount);
        txtClusterDetail = (TextView) findViewById(R.id.txt_cluster_detail);
        seekTime = (SeekBar) findViewById(R.id.seek_time);
        recClusters = (RecyclerView) findViewById(R.id.rec_clusters);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        txtSelectedTime = (TextView) findViewById(R.id.txtSelectedTime);

        String path = new File(this.getFilesDir(), Config.DATA_FILE_NAME).getAbsolutePath();
        edtFilepath.setText(path);

        progressBar.setVisibility(View.INVISIBLE);

        seekTime.setEnabled(false);
        seekTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSeekTime(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                loadTime(seekBar.getProgress());
            }
        });

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recClusters.setLayoutManager(layoutManager);
    }

    public void clickLoad(View v) {
        new ClusteringTask().execute(edtFilepath.getText().toString(), edtClusterAmount.getText().toString());
    }

    private void loadTime(int time) {
        int timeWithSecs = updateSeekTime(time);
        Map.Entry<Integer, Dataset> entry = clusteredData.floorEntry(timeWithSecs);
        if (entry != null) {
            Dataset dataset = entry.getValue();
            Log.d(TAG, dataset.get(0).toString());
            Log.d(TAG, dataset.get(dataset.size() - 1).toString());

            String[] strings = new String[dataset.size()];
            for (int i = 0; i < dataset.size(); i++) {
                strings[i] = (String) dataset.get(i).classValue();
            }
            adapter = new ClusterAdapter(strings);
            recClusters.setAdapter(adapter);
        }
    }

    private int updateSeekTime(int time) {
        int timeWithSecs = (time / 6) * 10000 + (time % 6) * 1000;
        txtSelectedTime.setText(String.format(Locale.US, "%02d:%02d", time / 6, time * 10 % 60));
        return timeWithSecs;
    }

    private class ClusteringTask extends AsyncTask<String, Void, Dataset[]> {

        private long startTime;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startTime = System.currentTimeMillis();
            progressBar.setVisibility(View.VISIBLE);
            seekTime.setEnabled(false);
        }

        @Override
        protected Dataset[] doInBackground(String... params) {
            try {
                return new Clustering().doCluster(params[0], Integer.parseInt(params[1]), 5);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Dataset[] datasets) {
            super.onPostExecute(datasets);
            if (datasets != null && datasets.length > 0) {
                seekTime.setEnabled(true);
                clusteredData.clear();
                for (Dataset dataset : datasets) {
                    clusteredData.put((int) Math.round(dataset.get(0).value(1)), dataset);
                }
            }
            progressBar.setVisibility(View.INVISIBLE);
            txtClusterDetail.setText(getString(R.string.clustering_time).replace("#", String.valueOf((System.currentTimeMillis() - startTime) / 1000.0)));
        }
    }
}
