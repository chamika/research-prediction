package com.chamika.research.smartprediction.prediction.processor;

import com.chamika.research.smartprediction.prediction.ClusteringDataMapper;
import com.chamika.research.smartprediction.prediction.Event;

import net.sf.javaml.core.Dataset;

import java.util.List;
import java.util.Map;

public interface ClusteredPredictionProvider {
    Map.Entry<Double, List<Dataset>> queryClusterDataset(List<Event> events);

    ClusteringDataMapper getDataMapper();
}
