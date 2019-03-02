package com.chamika.research.smartprediction.prediction;

import java.io.Serializable;
import java.util.Date;

public class Event implements Serializable {
    private Date date;

    public Event() {
        this(new Date());
    }

    public Event(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }
}
