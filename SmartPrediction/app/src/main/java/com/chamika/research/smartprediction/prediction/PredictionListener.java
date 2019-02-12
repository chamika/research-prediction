package com.chamika.research.smartprediction.prediction;

import java.util.List;

public interface PredictionListener {

    void onPredictions(List<Prediction> predictions);
}
