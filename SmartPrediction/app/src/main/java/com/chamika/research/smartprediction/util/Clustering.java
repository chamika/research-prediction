package com.chamika.research.smartprediction.util;


import android.util.Log;

import com.chamika.research.smartprediction.prediction.ClusteringDataMapper;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by chamika on 9/15/17.
 */

public class Clustering {

    private static String TAG = Clustering.class.getSimpleName();

    private Clusterer clusterer;

    public Clustering(Clusterer clusterer) {
        this.clusterer = clusterer;
    }

    private static boolean overlapped(Dataset cluster1, Dataset cluster2) {
        double r1 = calculateScalarValue(cluster1.get(0));
        double r2 = calculateScalarValue(cluster1.get(cluster1.size() - 1));
        double t1 = calculateScalarValue(cluster2.get(0));
        double t2 = calculateScalarValue(cluster2.get(cluster2.size() - 1));

        if (r1 == t1 || r1 == t2 || t1 == r2) {
            return true;
        } else if (r1 < t1 && r2 > t1) {
            return true;
        } else if (t1 < r1 && t2 > r1) {
            return true;
        } else if (r1 < t1 && r2 > t2) {
            return true;
        } else if (t1 < r1 && t2 > r2) {
            return true;
        }
        return false;

    }

    private static double calculateScalarValue(Instance o) {
        double sum = o.value(0) * 6 + o.value(1);
        if (o.noAttributes() >= 3) {
            sum += 100 * o.value(2);
        }
        return sum;
    }

    public Dataset[] doCluster(String fileAbsolutePath, ClusteringDataMapper dataMapper) throws IOException {
        /* Load a dataset */
        Dataset data = FileHandler.loadDataset(new File(fileAbsolutePath), dataMapper.getClassIndex(), ",");
        /* Create a new instance of the KMeans algorithm, with no options
         * specified. By default this will generate 4 clusters. */
        if (data.size() == 0) {
            Log.d(TAG, "clustering skipped. dataset is empty");
            return new Dataset[0];
        }
        Log.d(TAG, String.format("clustering started. clusterer:%s", clusterer.getClass().getSimpleName()));
        long start = System.currentTimeMillis();
        /* Cluster the data, it will be returned as an array of data sets, with
         * each dataset representing a cluster. */
        Dataset[] clusters = clusterer.cluster(data);
        Log.d(TAG, "clustering finished. count = " + clusters.length);
        Log.d(TAG, "clustering total time = " + (System.currentTimeMillis() - start) + " ms");

        //sort to make
        for (int i = 0; i < clusters.length; i++) {
//            Log.d(TAG, "Cluster " + i + " size:" + clusters[i].size());
            final int finalI = i;
            Collections.sort(clusters[i], new Comparator<Instance>() {
                @Override
                public int compare(Instance o1, Instance o2) {
                    // multiply bt 6 for day of week and multiply by 100000 is to maintain digits
                    return (int) Math.round((calculateScalarValue(o1) - (calculateScalarValue(o2))) * 100000);
                }
            });
        }

        for (int i = 0; i < clusters.length; i++) {
            Dataset cluster1 = clusters[i];
            if (cluster1.size() > 0) {
                for (int j = i + 1; j < clusters.length; j++) {
                    Dataset cluster2 = clusters[j];
                    if (cluster2.size() > 0) {
                        if (overlapped(cluster1, cluster2)) {
                            Log.d(TAG, "overlapped:");
                            Log.d(TAG, "Cluster  " + i + "->" + cluster1.get(0).toString());
                            Log.d(TAG, "Cluster  " + i + "->" + cluster1.get(cluster1.size() - 1).toString());
                            Log.d(TAG, "Cluster  " + j + "->" + cluster2.get(0).toString());
                            Log.d(TAG, "Cluster  " + j + "->" + cluster2.get(cluster2.size() - 1).toString());
                        }
                    }
                }
            }
        }

        return clusters;
    }
}
