package com.chamika.research.smartprediction.prediction;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.chamika.research.smartprediction.prediction.processor.KMeansPredictionProcessor;
import com.chamika.research.smartprediction.prediction.processor.PredictionProcessor;
import com.chamika.research.smartprediction.store.BaseStore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PredictionEngine {

    private static final String TAG = PredictionEngine.class.getSimpleName();
    public static final int EVENT_ACTIVITY_LOOKBACK_MINS = 10;

    private List<PredictionListener> predictionListeners;
    private PredictionProcessor processor;
    private Context context;

    public PredictionEngine(Context context) {
        predictionListeners = new ArrayList<>();
        this.context = context;
        processor = new KMeansPredictionProcessor(context);
//        processor = new EMPredictionProcessor(context);
        processor.init();
    }

    public void addPredictionListener(PredictionListener listener) {
        predictionListeners.add(listener);
    }

    public void removePredictionListener(PredictionListener listener) {
        predictionListeners.remove(listener);
    }

    public void addEvent(Event event) {
        if (event != null) {
            Log.d(TAG, event.toString() + " received.");
        } else {
            Log.d(TAG, "null event received. Ignore sending to prediction processor");
            return;
        }

        if (event.getData() == null) {
            Event activityEvent = getActivityEvent(event);
            if (activityEvent != null) {
                event.setData(activityEvent.getData());
            }
        }

        List<Prediction> predictions = processor.processEvent(event);
        notifyPredictions(predictions);
    }

    public List<Prediction> addEventSynchronous(Event event) {
        if (event != null) {
            Log.d(TAG, event.toString() + " received.");
        } else {
            Log.d(TAG, "null event received. Ignore sending to prediction processor");
            return null;
        }

        if (event.getData() == null) {
            Event activityEvent = getActivityEvent(event);
            if (activityEvent != null) {
                event.setData(activityEvent.getData());
            }
        }

        List<Prediction> predictions = processor.processEvent(event);
        Log.d(TAG, "Prediction count=" + predictions.size());
        savePredictions(processor.getPredictionProcessorId(), new Date(), predictions);
        return predictions;
    }

    public void refresh() {
        Log.d(TAG, "Refreshing predictions");
        processor.init();
    }

    private void notifyPredictions(List<Prediction> predictions) {
        Log.d(TAG, "Prediction count=" + predictions.size());
        if (!predictions.isEmpty()) {
            savePredictions(processor.getPredictionProcessorId(), new Date(), predictions);
            for (PredictionListener predictionListener : predictionListeners) {
                predictionListener.onPredictions(predictions);
            }
        }
    }

    private void savePredictions(int predictionProcessorId, Date time, List<Prediction> predictions) {
//        PredictionSave predictionSave = new PredictionSave(predictionProcessorId, time, predictions);
//        SavePredictionTask savePredictionTask = new SavePredictionTask(this.context);
//        savePredictionTask.execute(predictionSave);
        if (predictions != null) {
            long predictionId = System.currentTimeMillis();
            for (int i = 0; i < predictions.size(); i++) {
                Prediction prediction = predictions.get(i);
                if (this.context != null) {
                    BaseStore.savePrediction(this.context, predictionId, predictionProcessorId, i, prediction.getType().name(), time.getTime(), prediction.data);
                }
            }
        }
    }

    public Event getActivityEvent(Event event) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(event.getDate());
        cal.add(Calendar.MINUTE, -EVENT_ACTIVITY_LOOKBACK_MINS);

        long timeFrom = cal.getTimeInMillis();
        long timeTo = event.getDate().getTime();

        Cursor cursor = BaseStore.getEventsWithTypeAndTime(context, "ACT", timeFrom, timeTo);
        if (cursor.moveToFirst()) {
            Event activityEvent = new Event(new Date(cursor.getLong(cursor.getColumnIndex(BaseStore.EventsStructure.COLUMN_NAME_TIME))));
//            activityEvent.setData(cursor);
            activityEvent.setData(cursor.getString(cursor.getColumnIndex(BaseStore.EventsStructure.COLUMN_NAME_DATA1)));
            return activityEvent;
        }
        return null;
    }
}
