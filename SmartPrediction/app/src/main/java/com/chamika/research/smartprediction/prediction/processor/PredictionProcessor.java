package com.chamika.research.smartprediction.prediction.processor;

import android.content.Context;

import com.chamika.research.smartprediction.prediction.DataMapper;
import com.chamika.research.smartprediction.prediction.Event;
import com.chamika.research.smartprediction.prediction.Prediction;
import com.chamika.research.smartprediction.store.FileExport;
import com.chamika.research.smartprediction.util.Config;

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

    protected String prepareDataFile(Context context, DataMapper dataMapper) {
        String dataFileName = Config.DATA_FILE_NAME;
        if (context != null) {
            FileExport.exportDBtoFile(context, dataFileName, dataMapper);
        }
        return dataFileName;
    }

}
