package com.chamika.research.smartprediction.prediction;

import java.io.Serializable;

public abstract class Prediction implements Serializable {
    protected String data;
    private String id;
    private Type type;

    public Prediction(String id, Type type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        APP, CALL, SMS, ACT
    }
}
