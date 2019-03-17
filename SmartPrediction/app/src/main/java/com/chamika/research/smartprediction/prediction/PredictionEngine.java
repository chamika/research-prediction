package com.chamika.research.smartprediction.prediction;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.chamika.research.smartprediction.store.BaseStore;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PredictionEngine {

    private static final String TAG = PredictionEngine.class.getSimpleName();

    private static PredictionEngine INSTANCE;

    private List<PredictionListener> predictionListeners;
    private PredictionProcessor processor;
    private Context context;

    private PredictionEngine(Context context) {
        predictionListeners = new ArrayList<>();
        this.context = context;
        processor = new PredictionProcessor(context);
        processor.init();
    }

    public static PredictionEngine getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new PredictionEngine(context);
        }
        return INSTANCE;
    }

    public void addPredictionListener(PredictionListener listener) {
        predictionListeners.add(listener);
    }

    public void removePredictionListener(PredictionListener listener) {
        predictionListeners.remove(listener);
    }

    public void addEvent(Event event) {
        if (event != null) {
            Log.d(TAG, event.toString() + " received.");
        } else {
            Log.d(TAG, "null event received.");
        }

        List<Prediction> predictions = processor.processEvent(event);
        notifyPredictions(predictions);
    }

    public List<Prediction> addEventSynchronous(Event event) {
        if (event != null) {
            Log.d(TAG, event.toString() + " received.");
        } else {
            Log.d(TAG, "null event received.");
        }

        List<Prediction> predictions = processor.processEvent(event);
        Log.d(TAG, "Prediction count=" + predictions.size());
        savePredictions(processor.getPredictionProcessorId(), new Date(), predictions);
        return predictions;
    }

    private void notifyPredictions(List<Prediction> predictions) {
        Log.d(TAG, "Prediction count=" + predictions.size());
        if (!predictions.isEmpty()) {
            savePredictions(processor.getPredictionProcessorId(), new Date(), predictions);
            for (PredictionListener predictionListener : predictionListeners) {
                predictionListener.onPredictions(predictions);
            }
        }
    }

    private void savePredictions(int predictionProcessorId, Date time, List<Prediction> predictions) {
//        PredictionSave predictionSave = new PredictionSave(predictionProcessorId, time, predictions);
//        SavePredictionTask savePredictionTask = new SavePredictionTask(this.context);
//        savePredictionTask.execute(predictionSave);
        if (predictions != null) {
            long predictionId = System.currentTimeMillis();
            for (int i = 0; i < predictions.size(); i++) {
                Prediction prediction = predictions.get(i);
                if (this.context != null) {
                    BaseStore.savePrediction(this.context, predictionId, predictionProcessorId, i, prediction.getType().name(), time.getTime(), prediction.data);
                }
            }
        }
    }

    private static class SavePredictionTask extends AsyncTask<PredictionSave, Integer, Boolean> {
        private WeakReference<Context> contextRef;

        public SavePredictionTask(Context context) {
            this.contextRef = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(PredictionSave... predictionSaves) {
            if (predictionSaves != null && predictionSaves.length > 1) {
                for (PredictionSave predictionSave : predictionSaves) {
                    List<Prediction> predictions = predictionSave.getPredictions();
                    int predictionProcessorId = predictionSave.getPredictionProcessorId();
                    Date time = predictionSave.getPredictionTime();
                    long predictionId = System.currentTimeMillis();
                    if (predictions != null) {
                        for (int i = 0; i < predictions.size(); i++) {
                            Prediction prediction = predictions.get(i);
                            if (this.contextRef.get() != null) {
                                BaseStore.savePrediction(this.contextRef.get(), predictionId, predictionProcessorId, i, prediction.getType().name(), time.getTime(), prediction.data);
                            }
                        }
                    }
                }
                return true;
            } else {
                return false;
            }
        }

    }
}
