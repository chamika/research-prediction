package com.chamika.research.smartprediction.prediction;

import java.util.Date;
import java.util.List;

public class PredictionSave {
    private int predictionProcessorId;
    private Date predictionTime;
    private List<Prediction> predictions;

    public PredictionSave(int predictionProcessorId, Date predictionTime, List<Prediction> predictions) {
        this.predictionProcessorId = predictionProcessorId;
        this.predictionTime = predictionTime;
        this.predictions = predictions;
    }

    public int getPredictionProcessorId() {
        return predictionProcessorId;
    }

    public Date getPredictionTime() {
        return predictionTime;
    }

    public List<Prediction> getPredictions() {
        return predictions;
    }
}
