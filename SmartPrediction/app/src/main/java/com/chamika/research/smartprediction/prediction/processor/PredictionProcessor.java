package com.chamika.research.smartprediction.prediction.processor;

import com.chamika.research.smartprediction.prediction.Event;
import com.chamika.research.smartprediction.prediction.Prediction;

import java.util.List;

public abstract class PredictionProcessor {

    private static final String TAG = PredictionProcessor.class.getSimpleName();

    protected InitializationListener initializationListener;
    protected boolean initialized = false;

    public abstract void init();

    public abstract List<Prediction> processEvent(Event event);

    public void setInitializationListener(InitializationListener initializationListener) {
        this.initializationListener = initializationListener;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public abstract int getPredictionProcessorId();

    public interface InitializationListener {
        void onInitializedSuccess(long timeInMillis);

        void onInitializedFailed();
    }

}
