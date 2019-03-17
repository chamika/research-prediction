package com.chamika.research.smartprediction.prediction;

import java.io.Serializable;
import java.util.Date;

public class Event implements Serializable {
    private Date date;
    private String data;

    public Event() {
        this(new Date());
    }

    public Event(Date date) {
        assert date != null;
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
