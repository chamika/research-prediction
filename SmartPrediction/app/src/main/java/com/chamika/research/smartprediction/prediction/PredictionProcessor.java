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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class PredictionProcessor {
    private static int PREDICTION_PROCESSOR_ID = 1;

    //    public static final int DEFAULT_CLUSTER_COUNT = 48;
    public static final int DEFAULT_CLUSTER_COUNT = 48 * 7; // approximately 30 mins predictions
    public static final int CLUSTER_ITERATIONS = 1000;
    private static final String TAG = PredictionProcessor.class.getSimpleName();
    private static final int MAX_PREDICTIONS = 5;

    private Context context;
    private boolean initialized = false;
    private int clusterCount;
    private NavigableMap<Double, List<Dataset>> clusteredData = new TreeMap<>();
    private InitializationListener initializationListener;
    //    private ClusteringDataMapper dataMapper = new  new TimeBasedDataMapper();
    private ClusteringDataMapper dataMapper = new TimeActivityBasedDataMapper(10);

    public PredictionProcessor(Context context) {
        this.context = context;
        this.clusterCount = DEFAULT_CLUSTER_COUNT;
    }

    public PredictionProcessor(Context context, int clusterCount) {
        this.context = context;
        this.clusterCount = clusterCount;
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
        List<Event> events = new ArrayList<>();
        events.add(event);
        queryClusterPredictions(predictions, events);

        return predictions;
    }

    public void setInitializationListener(InitializationListener initializationListener) {
        this.initializationListener = initializationListener;
    }

    private void queryClusterPredictions(List<Prediction> predictions, List<Event> events) {
        if (!clusteredData.isEmpty()) {
            //TODO fix for multiple events
            Map.Entry<Double, List<Dataset>> entry = queryClusterDataset(events);
            if (entry != null) {
                HashMap<String, Integer> map = new HashMap<>();
                List<Dataset> datasets = entry.getValue();
                for (Dataset dataset : datasets) {
                    Log.d(TAG, dataset.get(0).toString());
                    Log.d(TAG, dataset.get(dataset.size() - 1).toString());
                    //SMS|17d50f1c|2018.03.03 at 12:17:23
//                int size = Math.min(dataset.size(), MAX_PREDICTIONS);
                    int size = dataset.size();
//                String[] strings = new String[size];
                    for (int i = 0; i < size; i++) {
                        String event = (String) dataset.get(i).classValue();
                        String[] split = event.split("\\|");
                        String key = split[0] + "|" + split[1];
                        Integer count = map.get(key);
                        if (count == null) {
                            map.put(key, 1);
                        } else {
                            map.put(key, count + 1);
                        }
                    }
                }

                //create predictions
                //limit predictions using map of counts
                SortedSet<Map.Entry<String, Integer>> sortedEntriesByValue = new TreeSet<>(
                        new Comparator<Map.Entry<String, Integer>>() {
                            @Override
                            public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
//                                return e1.getValue().compareTo(e2.getValue());
                                return e2.getValue().compareTo(e1.getValue());//descending order
                            }
                        }
                );

                sortedEntriesByValue.addAll(map.entrySet());

                Map<String, Integer> countMap = new HashMap<>();
                int count = 1;
                for (Map.Entry<String, Integer> mapEntry : sortedEntriesByValue) {
                    String event = mapEntry.getKey();
                    String[] split = event.split("\\|");
                    String eventType = split[0];

                    Integer typeCount = countMap.get(eventType);
                    if (typeCount == null) {
                        typeCount = 1;
                    } else {
                        typeCount++;
                    }
                    countMap.put(event, typeCount);
                    if (typeCount > MAX_PREDICTIONS) {
                        continue;
                    }

                    Prediction prediction = null;
                    if (EventType.ACT.text().equals(eventType)) {
                        prediction = new ActivityPrediction(String.valueOf(count++), split[1]);
                    } else if (EventType.CALL.text().equals(eventType)) {
                        prediction = new CallPrediction(String.valueOf(count++), split[1]);
                    } else if (EventType.SMS.text().equals(eventType)) {
                        prediction = new MessagePrediction(String.valueOf(count++), split[1]);
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

    public Map.Entry<Double, List<Dataset>> queryClusterDataset(List<Event> events) {
        double key = dataMapper.generateKey(events.get(0));
        return clusteredData.ceilingEntry(key);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public int getPredictionProcessorId() {
        return PREDICTION_PROCESSOR_ID;
    }

    public interface InitializationListener {
        void onInitializedSuccess(long timeInMillis);

        void onInitializedFailed();
    }

    private class ClusteringTask extends AsyncTask<Void, Void, Dataset[]> {

        private WeakReference<Context> contextRef;
        private long startTime;

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
                    FileExport.exportDBtoFile(context, dataFileName, dataMapper);
                }
                context = contextRef.get();
                if (context != null) {
                    String path = new File(context.getFilesDir(), Config.DATA_FILE_NAME).getAbsolutePath();
                    startTime = System.currentTimeMillis();
                    return new Clustering().doCluster(path, dataMapper, clusterCount, CLUSTER_ITERATIONS);
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
                long endTime = System.currentTimeMillis();
                clusteredData.clear();
                for (Dataset dataset : datasets) {
                    if (dataset.size() == 0) {
                        continue;
                    }
                    double key = dataMapper.generateKey(dataset);
                    List<Dataset> datasetList = clusteredData.get(key);
                    if (datasetList == null) {
                        datasetList = new ArrayList<>();
                        clusteredData.put(key, datasetList);
                    }
                    datasetList.add(dataset);
                }
                if (initializationListener != null) {
                    initializationListener.onInitializedSuccess(endTime - startTime);
                }
            } else {
                if (initializationListener != null) {
                    initializationListener.onInitializedFailed();
                }
            }
            PredictionProcessor.this.initialized = true;
        }
    }
}
