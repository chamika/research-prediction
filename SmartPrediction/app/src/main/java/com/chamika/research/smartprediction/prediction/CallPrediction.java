package com.chamika.research.smartprediction.prediction;

public class CallPrediction extends ContactPrediction {
    public CallPrediction(String id, String contactHash) {
        super(Type.CALL, id, contactHash);
    }
}
