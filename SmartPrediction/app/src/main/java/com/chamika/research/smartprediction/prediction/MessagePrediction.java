package com.chamika.research.smartprediction.prediction;

public class MessagePrediction extends ContactPrediction {
    public MessagePrediction(String id, String contactHash) {
        super(Type.SMS, id, contactHash);
    }
}
