package com.chamika.research.smartprediction.ui.accuracy;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.chamika.research.smartprediction.R;
import com.chamika.research.smartprediction.prediction.AppPrediction;
import com.chamika.research.smartprediction.prediction.ClusteringDataMapper;
import com.chamika.research.smartprediction.prediction.ContactPrediction;
import com.chamika.research.smartprediction.prediction.DataMapper;
import com.chamika.research.smartprediction.prediction.Event;
import com.chamika.research.smartprediction.prediction.Prediction;
import com.chamika.research.smartprediction.prediction.processor.ClusteredPredictionProvider;
import com.chamika.research.smartprediction.prediction.processor.EMPredictionProcessor;
import com.chamika.research.smartprediction.prediction.processor.KMeansPredictionProcessor;
import com.chamika.research.smartprediction.prediction.processor.PredictionProcessor;
import com.chamika.research.smartprediction.store.FileExport;
import com.chamika.research.smartprediction.util.Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class AccuracyActivity extends AppCompatActivity implements PredictionProcessor.InitializationListener {

    public static final String DATA_TRAINING_TXT = "data_training.txt";
    public static final String DATA_TEMP_TXT = "data_temp.txt";
    public static final String DATA_TEST_TXT = "data_test.txt";
    private static final String TAG = AccuracyActivity.class.getSimpleName();

    private EditText edtFilepath;
    private EditText edtClusterAmount;
    private EditText edtTestProbability;
    private EditText edtLookbackMins;
    private EditText edtTopK;
    private TextView txtClusterDetail;
    private TextView txtResults;
    private ProgressBar progressBar;
    private RadioButton radKmeans;
    private RadioButton radEM;
    private Button btnStart;

    private PredictionProcessor predictionProcessor;

    public static boolean isTraining(Random random, float testProbability) {
        int rand = random.nextInt(100);
        return rand >= testProbability * 100;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accuracy);

        init();
    }

    private void init() {
        edtFilepath = findViewById(R.id.edt_file_path);
        edtClusterAmount = findViewById(R.id.edt_cluster_amount);
        edtTestProbability = findViewById(R.id.edt_test_percentage);
        edtLookbackMins = findViewById(R.id.edt_lookback_mins);
        edtTopK = findViewById(R.id.edt_topk);
        txtClusterDetail = findViewById(R.id.txt_cluster_detail);
        txtResults = findViewById(R.id.txt_results);
        progressBar = findViewById(R.id.progress);
        radKmeans = findViewById(R.id.rad_kmeans);
        radEM = findViewById(R.id.rad_em);

        String path = new File(this.getFilesDir(), Config.DATA_FILE_NAME).getAbsolutePath();
        edtFilepath.setText(path);

        progressBar.setVisibility(View.INVISIBLE);

        btnStart = findViewById(R.id.btn_start);

        enableElements(false);

        progressBar.setVisibility(View.INVISIBLE);
    }

    public void clickLoad(View v) {
        final float testProbability = Float.parseFloat(edtTestProbability.getText().toString());
        if (radKmeans.isChecked()) {
            predictionProcessor = new KMeansPredictionProcessor(this, Integer.parseInt(edtClusterAmount.getText().toString())) {
                @Override
                protected String prepareDataFile(Context context, DataMapper dataMapper) {
                    return splitTrainingAndTestData(context, dataMapper, testProbability);
                }
            };
        } else if (radEM.isChecked()) {
            predictionProcessor = new EMPredictionProcessor(this, Integer.parseInt(edtClusterAmount.getText().toString())) {
                @Override
                protected String prepareDataFile(Context context, DataMapper dataMapper) {
                    return splitTrainingAndTestData(context, dataMapper, testProbability);
                }
            };
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

    private void enableElements(boolean enable) {
        btnStart.setEnabled(enable);
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

    private String splitTrainingAndTestData(Context context, DataMapper dataMapper, float testProbability) {
        String dataTempFileName = DATA_TEMP_TXT;
        String dataTrainingFileName = DATA_TRAINING_TXT;
        String dataTestFileName = DATA_TEST_TXT;
        if (context == null) {
            return null;
        }
        FileExport.exportDBtoFile(context, dataTempFileName, dataMapper);
        File filesDir = context.getFilesDir();

        try (BufferedReader bIn = new BufferedReader(new FileReader(new File(filesDir, dataTempFileName)));
             BufferedWriter bOutTraining = new BufferedWriter(new FileWriter(new File(filesDir, dataTrainingFileName)));
             BufferedWriter bOutTest = new BufferedWriter(new FileWriter(new File(filesDir, dataTestFileName)))
        ) {
            String readLine = "";
            Random random = new Random();
            while ((readLine = bIn.readLine()) != null) {
                boolean isTraining = isTraining(random, testProbability);
                BufferedWriter writer = isTraining ? bOutTraining : bOutTest;
                writer.write(readLine);
                writer.newLine();
            }

        } catch (IOException e) {
            Log.e(TAG, "Error in file reading or writing", e);
        }

        return dataTrainingFileName;
    }

    public void startTest(View view) {
        int topK = Integer.parseInt(edtTopK.getText().toString());
        int lookBackMins = Integer.parseInt(edtLookbackMins.getText().toString());
        new AccuracyTestRunner(this, topK, lookBackMins).execute(predictionProcessor);
    }

    private void updateTestResults(TestResult testResult) {
        txtResults.setText(testResult.toString());
    }

    private static class AccuracyTestRunner extends AsyncTask<PredictionProcessor, Integer, TestResult> {

        private WeakReference<Context> contextRef;
        private int topK;
        private int lookbackMins;

        public AccuracyTestRunner(Context context, int topK, int lookbackMins) {
            this.contextRef = new WeakReference<>(context);
            this.topK = topK;
            this.lookbackMins = lookbackMins;
        }

        @Override
        protected TestResult doInBackground(PredictionProcessor... processors) {
            if (processors == null || processors.length == 0) {
                return TestResult.error("No prediction processor found");
            }
            PredictionProcessor predictionProcessor = processors[0];
            if (!(predictionProcessor instanceof ClusteredPredictionProvider)) {
                return TestResult.error("Prediction processor is not a ClusteredPredictionProvider for reverse mapping");
            }

            File filesDir = contextRef.get().getFilesDir();

            int testCount = 0;
            float totalScore = 0;
            long duration = System.currentTimeMillis();

            try (BufferedReader bIn = new BufferedReader(new FileReader(new File(filesDir, DATA_TEST_TXT)))) {
                String readLine = "";
                Calendar calendar = Calendar.getInstance();
                while ((readLine = bIn.readLine()) != null) {
                    ClusteringDataMapper dataMapper = ((ClusteredPredictionProvider) predictionProcessor).getDataMapper();
                    Event mappedEvent = dataMapper.reverseKey(readLine);
                    calendar.setTime(mappedEvent.getDate());
                    calendar.add(Calendar.MINUTE, -lookbackMins);
                    Event event = new Event(calendar.getTime());
                    event.setData(mappedEvent.getData());
                    //APP|com.facebook.katana|2019.10.29 at 08:56:27
                    String[] classSplits = readLine.split(",")[dataMapper.getClassIndex()].split("\\|");
                    Prediction.Type type = Prediction.Type.valueOf(classSplits[0]);
                    List<Prediction> predictions = predictionProcessor.processEvent(event);
                    List<Prediction> filteredPredictions = new ArrayList<>();
                    for (Prediction prediction : predictions) {
                        if (filteredPredictions.size() >= topK) {
                            break;
                        }
                        if (prediction.getType() == type) {
                            filteredPredictions.add(prediction);
                        }
                    }

                    float score = 0f;
                    for (int i = 0; i < filteredPredictions.size(); i++) {
                        Prediction filteredPrediction = filteredPredictions.get(i);
                        boolean matched = false;
                        String clazz = classSplits[1];
                        if (filteredPrediction instanceof ContactPrediction && ((ContactPrediction) filteredPrediction).getEncryptedNumber().equals(clazz)) {
                            matched = true;
                        } else if (filteredPrediction instanceof AppPrediction && ((AppPrediction) filteredPrediction).getPackageName().equals(clazz)) {
                            matched = true;
                        }

                        if (matched) {
                            score = (topK - i) / (topK * 1f);
                            break;
                        }
                    }

                    totalScore += score;
                    testCount++;
                }

            } catch (IOException e) {
                Log.e(TAG, "Error in file reading", e);
                return TestResult.error("Error in file reading test data");
            }
            duration = System.currentTimeMillis() - duration;

            return TestResult.success(testCount, duration, totalScore / testCount * 1f, topK);
        }

        @Override
        protected void onPostExecute(TestResult testResult) {
            super.onPostExecute(testResult);
            Context context = contextRef.get();
            if (context instanceof AccuracyActivity) {
                ((AccuracyActivity) context).updateTestResults(testResult);
            }
        }
    }
}
