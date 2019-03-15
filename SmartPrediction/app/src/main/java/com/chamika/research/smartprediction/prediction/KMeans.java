//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.chamika.research.smartprediction.prediction;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.tools.DatasetTools;

import java.util.Random;

public class KMeans implements Clusterer {
    private int numberOfClusters;
    private int numberOfIterations;
    private Random rg;
    private DistanceMeasure dm;
    private Instance[] centroids;

    public KMeans() {
        this(4);
    }

    public KMeans(int k) {
        this(k, 100);
    }

    public KMeans(int clusters, int iterations) {
        this(clusters, iterations, new EuclideanDistance());
    }

    public KMeans(int clusters, int iterations, DistanceMeasure dm) {
        this.numberOfClusters = -1;
        this.numberOfIterations = -1;
        this.numberOfClusters = clusters;
        this.numberOfIterations = iterations;
        this.dm = dm;
        this.rg = new Random(System.currentTimeMillis());
    }

    public Dataset[] cluster(Dataset data) {
        if (data.size() == 0) {
            throw new RuntimeException("The dataset should not be empty");
        } else if (this.numberOfClusters == 0) {
            throw new RuntimeException("There should be at least one cluster");
        } else {
            Instance min = DatasetTools.minAttributes(data);
            Instance max = DatasetTools.maxAttributes(data);
            this.centroids = new Instance[this.numberOfClusters];
            int instanceLength = data.instance(0).noAttributes();

            int j;
            for (j = 0; j < this.numberOfClusters; ++j) {
                double[] randomInstance = new double[instanceLength];

                for (int i = 0; i < instanceLength; ++i) {
                    double dist = Math.abs(max.value(i) - min.value(i));
                    randomInstance[i] = (double) ((float) (min.value(i) + this.rg.nextDouble() * dist));
                }

                this.centroids[j] = new DenseInstance(randomInstance);
            }

            int iteration = 0;
            boolean centroidsChanged = true;
            boolean randomCentroids = true;

            int i;
            int tmpCluster;
            double minDistance;
            double dist;
            while (iteration < this.numberOfIterations && centroidsChanged) {
                ++iteration;
                int[] assignment = new int[data.size()];

                for (i = 0; i < data.size(); ++i) {
                    tmpCluster = 0;
                    minDistance = this.dm.measure(this.centroids[0], data.instance(i));

                    for (j = 1; j < this.centroids.length; ++j) {
                        dist = this.dm.measure(this.centroids[j], data.instance(i));
                        if (this.dm.compare(dist, minDistance)) {
                            minDistance = dist;
                            tmpCluster = j;
                        }
                    }

                    assignment[i] = tmpCluster;
                }

                double[][] sumPosition = new double[this.numberOfClusters][instanceLength];
                int[] countPosition = new int[this.numberOfClusters];

                for (i = 0; i < data.size(); ++i) {
                    Instance in = data.instance(i);

                    for (j = 0; j < instanceLength; ++j) {
                        sumPosition[assignment[i]][j] += in.value(j);
                    }

                    ++countPosition[assignment[i]];
                }

                centroidsChanged = false;
                randomCentroids = false;

                for (i = 0; i < this.numberOfClusters; ++i) {
                    double[] randomInstance;
                    if (countPosition[i] > 0) {
                        randomInstance = new double[instanceLength];

                        for (j = 0; j < instanceLength; ++j) {
                            randomInstance[j] = (double) ((float) sumPosition[i][j] / (float) countPosition[i]);
                        }

                        Instance newCentroid = new DenseInstance(randomInstance);
                        if (this.dm.measure(newCentroid, this.centroids[i]) > 1.0E-4D) {
                            centroidsChanged = true;
                            this.centroids[i] = newCentroid;
                        }
                    } else {
                        randomInstance = new double[instanceLength];

                        for (j = 0; j < instanceLength; ++j) {
                            dist = Math.abs(max.value(j) - min.value(j));
                            randomInstance[j] = (double) ((float) (min.value(j) + this.rg.nextDouble() * dist));
                        }

                        randomCentroids = true;
                        this.centroids[i] = new DenseInstance(randomInstance);
                    }
                }
            }

            Dataset[] output = new Dataset[this.centroids.length];

            for (i = 0; i < this.centroids.length; ++i) {
                output[i] = new DefaultDataset();
            }

            for (i = 0; i < data.size(); ++i) {
                tmpCluster = 0;
                minDistance = this.dm.measure(this.centroids[0], data.instance(i));

                for (j = 0; j < this.centroids.length; ++j) {
                    dist = this.dm.measure(this.centroids[j], data.instance(i));
                    if (this.dm.compare(dist, minDistance)) {
                        minDistance = dist;
                        tmpCluster = j;
                    }
                }

                output[tmpCluster].add(data.instance(i));
            }

            return output;
        }
    }
}
