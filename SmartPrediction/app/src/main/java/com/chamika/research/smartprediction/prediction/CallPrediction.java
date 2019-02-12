package com.chamika.research.smartprediction.prediction;

public class CallPrediction extends Prediction {
    public CallPrediction(String id, String number) {
        super(id, Type.CALL);
        this.data = number;
    }

    public String getNumber() {
        return data;
    }
}
