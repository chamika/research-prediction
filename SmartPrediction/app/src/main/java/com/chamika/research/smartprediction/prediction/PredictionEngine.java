package com.chamika.research.smartprediction.prediction;

import java.util.List;

public interface PredictionEngine {

    List<Prediction> processEventSynchronous(Event event);

    void processEvent(Event event);

    interface PredictionListener {

        void onPredictions(List<Prediction> predictions);
    }
}

