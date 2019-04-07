package com.chamika.research.smartprediction.prediction;

public class ContactPrediction extends Prediction {
    private String name;
    private String number;
    private String uri;


    public ContactPrediction(Type type, String id, String contactHash) {
        super(id, type);
        this.data = contactHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getEncryptedNumber() {
        return data;
    }
}
