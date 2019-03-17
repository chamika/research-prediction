package com.chamika.research.smartprediction.prediction;

import net.sf.javaml.core.Dataset;

public interface ClusteringDataMapper extends DataMapper {

    int getClassIndex();

    double generateKey(Dataset dataset);
    
    double generateKey(Event event);
}
