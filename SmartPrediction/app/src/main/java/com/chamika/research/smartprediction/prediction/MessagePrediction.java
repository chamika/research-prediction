package com.chamika.research.smartprediction.prediction;

public class MessagePrediction extends Prediction {
    public MessagePrediction(String id, String number) {
        super(id, Type.SMS);
        this.data = number;
    }

    public String getNumber() {
        return data;
    }
}
