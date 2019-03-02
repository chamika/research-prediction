package com.chamika.research.smartprediction.ui.hover;

public class Contact {
    private String name;
    private String number;
    private String uri;

    public Contact(String name, String number, String uri) {
        this.name = name;
        this.number = number;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public String getNumber() {
        return number;
    }
}
