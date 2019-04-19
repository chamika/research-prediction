package com.chamika.research.smartprediction.ui.results;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.chamika.research.smartprediction.R;
import com.chamika.research.smartprediction.prediction.Event;
import com.chamika.research.smartprediction.prediction.processor.ClusteredPredictionProvider;
import com.chamika.research.smartprediction.prediction.processor.EMPredictionProcessor;
import com.chamika.research.smartprediction.prediction.processor.KMeansPredictionProcessor;
import com.chamika.research.smartprediction.prediction.processor.PredictionProcessor;
import com.chamika.research.smartprediction.util.Config;

import net.sf.javaml.core.Dataset;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ResultsActivity extends AppCompatActivity implements PredictionProcessor.InitializationListener, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    public static final String TAG = ResultsActivity.class.getSimpleName();
    private EditText edtFilepath;
    private EditText edtClusterAmount;
    private TextView txtClusterDetail;
    private RecyclerView recClusters;
    private ProgressBar progressBar;
    private Button btnDate;
    private Button btnTime;
    private RadioButton radKmeans;
    private RadioButton radEM;

    private LinearLayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private PredictionProcessor predictionProcessor;

    private Calendar selectedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        init();
    }

    private void init() {
        selectedTime = Calendar.getInstance();
        selectedTime.setTime(new Date());

        edtFilepath = (EditText) findViewById(R.id.edt_file_path);
        edtClusterAmount = (EditText) findViewById(R.id.edt_cluster_amount);
        txtClusterDetail = (TextView) findViewById(R.id.txt_cluster_detail);
        recClusters = (RecyclerView) findViewById(R.id.rec_clusters);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        btnDate = findViewById(R.id.btn_date);
        btnTime = findViewById(R.id.btn_time);
        radKmeans = findViewById(R.id.rad_kmeans);
        radEM = findViewById(R.id.rad_em);

        String path = new File(this.getFilesDir(), Config.DATA_FILE_NAME).getAbsolutePath();
        edtFilepath.setText(path);

        progressBar.setVisibility(View.INVISIBLE);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recClusters.setLayoutManager(layoutManager);

        enableElements(false);
        updateDateTime();

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cal = ResultsActivity.this.selectedTime;
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(ResultsActivity.this, ResultsActivity.this, hour, minute, false);
                timePickerDialog.show();
            }
        });

        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = selectedTime;
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // Create a new instance of DatePickerDialog and return it
                DatePickerDialog datePickerDialog = new DatePickerDialog(ResultsActivity.this, ResultsActivity.this, year, month, day);
                datePickerDialog.show();
            }
        });
    }

    public void clickLoad(View v) {
        if (radKmeans.isChecked()) {
            predictionProcessor = new KMeansPredictionProcessor(this, Integer.parseInt(edtClusterAmount.getText().toString()));
        } else if (radEM.isChecked()) {
            predictionProcessor = new EMPredictionProcessor(this, Integer.parseInt(edtClusterAmount.getText().toString()));
        }
        if (predictionProcessor != null) {
            predictionProcessor.setInitializationListener(this);
            predictionProcessor.init();

            progressBar.setVisibility(View.VISIBLE);
            enableElements(false);
        } else {
            Toast.makeText(this, "No Clustering algorithm selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadData() {
        if (predictionProcessor != null && predictionProcessor.isInitialized() && predictionProcessor instanceof ClusteredPredictionProvider) {
            Event event = new Event(selectedTime.getTime());
            List<Event> events = new ArrayList<>();
            events.add(event);
            Map.Entry<Double, List<Dataset>> entry = ((ClusteredPredictionProvider) predictionProcessor).queryClusterDataset(events);

            if (entry != null) {
                List<String> allData = new ArrayList<>();

                List<Dataset> datasetList = entry.getValue();
                if (datasetList != null) {
                    for (Dataset dataset : datasetList) {
                        for (int i = 0; i < dataset.size(); i++) {
                            allData.add((String) dataset.get(i).classValue());
                        }
                    }
                }

                adapter = new ClusterAdapter(allData);
                recClusters.setAdapter(adapter);
            }
        } else {
            Toast.makeText(this, "Prediction processor is not ready", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInitializedSuccess(long timeInMillis) {
        progressBar.setVisibility(View.INVISIBLE);
        enableElements(true);
        txtClusterDetail.setText(getString(R.string.clustering_time).replace("#", String.valueOf(timeInMillis / 1000.0)));
    }

    @Override
    public void onInitializedFailed() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void enableElements(boolean enable) {
        btnDate.setEnabled(enable);
        btnTime.setEnabled(enable);
    }

    private void updateDateTime() {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");

        Date time = selectedTime.getTime();
        btnDate.setText(sdf1.format(time));
        btnTime.setText(sdf2.format(time));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        selectedTime.set(Calendar.MINUTE, minute);
        updateDateTime();
        loadData();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        selectedTime.set(Calendar.YEAR, year);
        selectedTime.set(Calendar.MONTH, month);
        selectedTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateDateTime();
        loadData();
    }
}
