package com.chamika.research.smartprediction.prediction;

public class ActivityPrediction extends Prediction {

    public ActivityPrediction(String id, String activity) {
        super(id, Type.ACT);
        this.data = activity;
    }

    public String getActivity() {
        return data;
    }
}
