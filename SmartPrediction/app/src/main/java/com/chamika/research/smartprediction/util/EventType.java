package com.chamika.research.smartprediction.util;

public enum EventType {
    ACT(1, "ACT"), CALL(3, "CALL"), SMS(4, "SMS"), APP(5, "APP");
    private int index;
    private String text;

    EventType(int index, String text) {
        this.index = index;
        this.text = text;
    }

    public int index() {
        return index;
    }

    public String text() {
        return text;
    }
}
