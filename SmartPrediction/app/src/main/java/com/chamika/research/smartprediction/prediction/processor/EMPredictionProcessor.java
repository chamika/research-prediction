package com.chamika.research.smartprediction.prediction.processor;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.chamika.research.smartprediction.prediction.ActivityPrediction;
import com.chamika.research.smartprediction.prediction.AppPrediction;
import com.chamika.research.smartprediction.prediction.CallPrediction;
import com.chamika.research.smartprediction.prediction.ClusteringDataMapper;
import com.chamika.research.smartprediction.prediction.Event;
import com.chamika.research.smartprediction.prediction.MessagePrediction;
import com.chamika.research.smartprediction.prediction.Prediction;
import com.chamika.research.smartprediction.prediction.TimeActivityBasedDataMapper;
import com.chamika.research.smartprediction.util.Clustering;
import com.chamika.research.smartprediction.util.Collections;
import com.chamika.research.smartprediction.util.Config;
import com.chamika.research.smartprediction.util.EventType;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.tools.weka.WekaClusterer;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import weka.clusterers.EM;

public class EMPredictionProcessor extends PredictionProcessor implements ClusteredPredictionProvider {
    public static final int DEFAULT_CLUSTER_COUNT = 48 * 7; // approximately 30 mins predictions
    public static final int CLUSTER_ITERATIONS = 1000;
    private static final String TAG = EMPredictionProcessor.class.getSimpleName();
    private static int PREDICTION_PROCESSOR_ID = 2;
    private Context context;
    private int clusterCount;
    private NavigableMap<Double, List<Dataset>> clusteredData = new TreeMap<>();
    private NavigableMap<Double, List<Prediction>> predictionData = new TreeMap<>();
    private ClusteringDataMapper dataMapper = new TimeActivityBasedDataMapper(10);

    public EMPredictionProcessor(Context context) {
        this.context = context;
        this.clusterCount = DEFAULT_CLUSTER_COUNT;
    }

    public EMPredictionProcessor(Context context, int clusterCount) {
        this.context = context;
        this.clusterCount = clusterCount;
    }

    @Override
    public void init() {
        initialized = false;
        new ClusteringTask(context).execute();
    }

    @Override
    public List<Prediction> processEvent(Event event) {
        List<Prediction> predictions = new ArrayList<>();
        if (!initialized) {
            Log.d(TAG, "Not initialized");
//            long l = System.currentTimeMillis();
//            if (l % 3 == 0) {
//                predictions.add(new AppPrediction("1", "com.facebook.orca"));
//            } else if (l % 3 == 1) {
//                predictions.add(new AppPrediction("1", "com.chamika.screenoff"));
//            } else {
//                predictions.add(new AppPrediction("1", "com.whatsapp"));
//            }
//            predictions.add(new CallPrediction("4", "3ea0ea2f"));
//            predictions.add(new CallPrediction("5", "3ea0ea2f"));
//            predictions.add(new MessagePrediction("6", "3ea0ea2f"));
            return predictions;
        }
        //TODO implement
        List<Event> events = new ArrayList<>();
        events.add(event);
        queryClusterPredictions(predictions, events);

        return predictions;
    }

    private void queryClusterPredictions(List<Prediction> predictions, List<Event> events) {
        if (!predictionData.isEmpty()) {
            Map.Entry<Double, List<Prediction>> entry = queryPredictions(events);
            if (entry != null) {
                predictions.addAll(entry.getValue());
            }
        }
    }

    @Override
    public Map.Entry<Double, List<Dataset>> queryClusterDataset(List<Event> events) {
        double key = dataMapper.generateKey(events.get(0));
        return clusteredData.floorEntry(key);
    }

    @Override
    public ClusteringDataMapper getDataMapper() {
        return dataMapper;
    }

    public Map.Entry<Double, List<Prediction>> queryPredictions(List<Event> events) {
        double key = dataMapper.generateKey(events.get(0));
        return predictionData.floorEntry(key);
    }

    @Override
    public int getPredictionProcessorId() {
        return PREDICTION_PROCESSOR_ID;
    }

    private void createPredictions(List<Prediction> predictions, List<Dataset> datasets) {
        HashMap<String, Integer> eventCounts = new HashMap<>();
        for (Dataset dataset : datasets) {
            Log.d(TAG, dataset.get(0).toString());
            Log.d(TAG, dataset.get(dataset.size() - 1).toString());
            //SMS|17d50f1c|2018.03.03 at 12:17:23
            int size = dataset.size();
            for (int i = 0; i < size; i++) {
                String event = (String) dataset.get(i).classValue();
                String[] split = event.split("\\|");
                String key = split[0] + "|" + split[1];
                Integer count = eventCounts.get(key);
                if (count == null) {
                    eventCounts.put(key, 1);
                } else {
                    eventCounts.put(key, count + 1);
                }
            }
        }

        //create predictions
        final Map<String, Integer> sortedEntriesByValue = Collections.sortByValue(eventCounts);

        int count = 1;
        for (Map.Entry<String, Integer> mapEntry : sortedEntriesByValue.entrySet()) {
            String event = mapEntry.getKey();
            String[] split = event.split("\\|");
            String eventType = split[0];

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
                String dataFileName = prepareDataFile(context, dataMapper);
                context = contextRef.get();
                if (context != null) {
                    String path = new File(context.getFilesDir(), dataFileName).getAbsolutePath();
                    startTime = System.currentTimeMillis();
                    //EM clustering
                    EM em = new EM();
                    try {
                        em.setNumClusters(clusterCount);
                        em.setMaxIterations(CLUSTER_ITERATIONS);
                    } catch (Exception e) {
                        Log.e(TAG, "Error in setting em options", e);
                    }
                    Dataset[] datasets = new Clustering(new WekaClusterer(em)).doCluster(path, dataMapper);

                    //prepare clustered data
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

                    //prepare predictionData
                    predictionData.clear();
                    for (Map.Entry<Double, List<Dataset>> clusteredEntry : clusteredData.entrySet()) {
                        ArrayList<Prediction> predictions = new ArrayList<>();
                        createPredictions(predictions, clusteredEntry.getValue());
                        predictionData.put(clusteredEntry.getKey(), predictions);
                    }

                    if (!Config.DEMO) {
                        clusteredData.clear();
                    }
                    return datasets;
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
                if (initializationListener != null) {
                    initializationListener.onInitializedSuccess(endTime - startTime);
                }
            } else {
                if (initializationListener != null) {
                    initializationListener.onInitializedFailed();
                }
            }
            EMPredictionProcessor.this.initialized = true;
        }
    }
}
