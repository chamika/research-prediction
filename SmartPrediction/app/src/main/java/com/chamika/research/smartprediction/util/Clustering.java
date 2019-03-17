package com.chamika.research.smartprediction.util;


import android.util.Log;

import com.chamika.research.smartprediction.prediction.ClusteringDataMapper;
import com.chamika.research.smartprediction.prediction.KMeans;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.evaluation.ClusterEvaluation;
import net.sf.javaml.clustering.evaluation.SumOfSquaredErrors;
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

    private static boolean overlapped(Dataset cluster1, Dataset cluster2) {
        int index = 1;
        double r1 = cluster1.get(0).value(index);
        double r2 = cluster1.get(cluster1.size() - 1).value(index);
        double t1 = cluster2.get(0).value(index);
        double t2 = cluster2.get(cluster2.size() - 1).value(index);

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

    public Dataset[] doCluster(String fileAbsolutePath, ClusteringDataMapper dataMapper, int clusterCount, int iterations) throws IOException {
        /* Load a dataset */
        Dataset data = FileHandler.loadDataset(new File(fileAbsolutePath), dataMapper.getClassIndex(), ",");
        /* Create a new instance of the KMeans algorithm, with no options
         * specified. By default this will generate 4 clusters. */
        Log.d(TAG, String.format("clustering started. clusterCount=%d iterations=%d", clusterCount, iterations));
        Clusterer km = new KMeans(clusterCount, iterations);
        /* Cluster the data, it will be returned as an array of data sets, with
         * each dataset representing a cluster. */
        Dataset[] clusters = km.cluster(data);
        Log.d(TAG, "clustering finished. count = " + clusters.length);
        ClusterEvaluation sse = new SumOfSquaredErrors();
//        Log.d(TAG,"score = " + sse.score(clusters));
        for (int i = 0; i < clusters.length; i++) {
            Log.d(TAG, "Cluster " + i + " size:" + clusters[i].size());
            Collections.sort(clusters[i], new Comparator<Instance>() {
                @Override
                public int compare(Instance o1, Instance o2) {
                    return (int) Math.round(o1.value(1) * 100 - o2.value(1) * 100);
                }
            });
        }
        for (int i = 0; i < clusters.length; i++) {
            Log.d(TAG, "**** Cluster " + i + "****");
            Dataset cluster = clusters[i];
//            for (Instance instance : cluster) {
//                Log.d(TAG,"Cluster  " + i + "->" + instance.toString());
//            }
            if (cluster.size() > 0) {
                Log.d(TAG, "Cluster  " + i + "->" + cluster.get(0).toString());
                Log.d(TAG, "Cluster  " + i + "->" + cluster.get(cluster.size() - 1).toString());
            }
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
