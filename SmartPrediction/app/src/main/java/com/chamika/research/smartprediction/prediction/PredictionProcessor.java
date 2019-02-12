package com.chamika.research.smartprediction.prediction;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.chamika.research.smartprediction.store.FileExport;
import com.chamika.research.smartprediction.util.Clustering;
import com.chamika.research.smartprediction.util.Config;
import com.chamika.research.smartprediction.util.EventType;

import net.sf.javaml.core.Dataset;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public class PredictionProcessor {
    //    public static final int CLUSTER_COUNT = 144;
    public static final int CLUSTER_COUNT = 48; // approximately 30 mins predictions
    //    public static final int CLUSTER_COUNT = 4;
    public static final int CLUSTER_ITERATIONS = 2;
    private static final String TAG = PredictionProcessor.class.getSimpleName();
    private static final int MAX_PREDICTIONS = 5;
    private Context context;
    private boolean initialized = false;

    private NavigableMap<Integer, Dataset> clusteredData = new TreeMap<>();

    public PredictionProcessor(Context context) {
        this.context = context;
    }

    public void init() {
        new ClusteringTask(context).execute();
    }

    public List<Prediction> processEvent(Event event) {
        List<Prediction> predictions = new ArrayList<>();
        if (!initialized) {
            Log.d(TAG, "Not initialized");
            return predictions;
        }
        //TODO implement
        if (event instanceof Event) {
//            AppPrediction appPrediction = new AppPrediction();
//            appPrediction.setPackageName("com.facebook.katana");
//            appPrediction.setId("2");
//            predictions.add(appPrediction);

            queryClusterPredictions(predictions);
        }

        return predictions;
    }

    private void queryClusterPredictions(List<Prediction> predictions) {
        if (!clusteredData.isEmpty()) {
            String timeWithSecs = new SimpleDateFormat("HHmmss").format(new Date());
            Map.Entry<Integer, Dataset> entry = clusteredData.ceilingEntry(Integer.parseInt(timeWithSecs));
            if (entry != null) {
                Set<String> set = new HashSet<>();
                Dataset dataset = entry.getValue();
                Log.d(TAG, dataset.get(0).toString());
                Log.d(TAG, dataset.get(dataset.size() - 1).toString());
                //SMS|17d50f1c|2018.03.03 at 12:17:23
//                int size = Math.min(dataset.size(), MAX_PREDICTIONS);
                int size = dataset.size();
//                String[] strings = new String[size];
                for (int i = 0; i < size; i++) {
                    String event = (String) dataset.get(i).classValue();
                    String[] split = event.split("\\|");
                    set.add(split[0] + "|" + split[1]);
                }

                //TODO create predictions
//                    adapter = new ClusterAdapter(strings);
//                    recClusters.setAdapter(adapter);
                int count = 1;
                for (String event : set) {
                    String[] split = event.split("\\|");
                    String eventType = split[0];
                    Prediction prediction = null;
                    if (EventType.ACT.text().equals(eventType)) {
                        prediction = new ActivityPrediction(String.valueOf(count++), split[1]);
                    } else if (EventType.CALL.text().equals(eventType)) {

                    } else if (EventType.SMS.text().equals(eventType)) {

                    } else if (EventType.APP.text().equals(eventType)) {
                        prediction = new AppPrediction(String.valueOf(count++), split[1]);
                    }

                    if (prediction != null) {
                        predictions.add(prediction);
                    }
                }

            }
        }
    }

    private class ClusteringTask extends AsyncTask<Void, Void, Dataset[]> {

        private WeakReference<Context> contextRef;

        public ClusteringTask(Context context) {
            this.contextRef = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Dataset[] doInBackground(Void... params) {
            try {
                Context context = contextRef.get();
                String dataFileName = Config.DATA_FILE_NAME;
                if (context != null) {
                    FileExport.exportDBtoFile(context, dataFileName);
                }
                context = contextRef.get();
                if (context != null) {
                    String path = new File(context.getFilesDir(), Config.DATA_FILE_NAME).getAbsolutePath();
                    return new Clustering().doCluster(path, CLUSTER_COUNT, CLUSTER_ITERATIONS);
                }
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Dataset[] datasets) {
            super.onPostExecute(datasets);
            if (datasets != null && datasets.length > 0) {
                clusteredData.clear();
                for (Dataset dataset : datasets) {
                    clusteredData.put((int) Math.round(dataset.get(0).value(1)), dataset);
                }
            }
            PredictionProcessor.this.initialized = true;
        }
    }

}
