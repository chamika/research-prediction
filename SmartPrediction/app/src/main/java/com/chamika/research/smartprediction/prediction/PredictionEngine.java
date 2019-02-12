package com.chamika.research.smartprediction.prediction;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PredictionEngine {

    private static final String TAG = PredictionEngine.class.getSimpleName();

    private static PredictionEngine INSTANCE;

    private List<PredictionListener> predictionListeners;
    private PredictionProcessor processor;

    private PredictionEngine(Context context) {
        predictionListeners = new ArrayList<>();
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

    private void notifyPredictions(List<Prediction> predictions) {
        if (predictions != null && !predictions.isEmpty()) {
            for (PredictionListener predictionListener : predictionListeners) {
                predictionListener.onPredictions(predictions);
            }
        }
    }
}
