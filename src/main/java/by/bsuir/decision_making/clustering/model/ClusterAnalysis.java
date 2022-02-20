package by.bsuir.decision_making.clustering.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ejml.data.DMatrixRMaj;
import org.ejml.simple.SimpleBase;
import org.ejml.simple.SimpleMatrix;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ClusterAnalysis {

    public static final DistanceMethod<double[]> EuclideanDistance = (valuesA, valuesB) -> {
        if (valuesA.length != valuesB.length) {
            throw new IllegalArgumentException(String.format("Values are of different dimensions: %d, %d",
                                                             valuesA.length, valuesB.length));
        }
        DMatrixRMaj rawVectorObservationA = DMatrixRMaj.wrap(1, valuesA.length, valuesA);
        DMatrixRMaj rawVectorObservationB = DMatrixRMaj.wrap(1, valuesB.length, valuesB);
        SimpleMatrix vectorObservationA = SimpleMatrix.wrap(rawVectorObservationA);
        SimpleMatrix vectorObservationB = SimpleMatrix.wrap(rawVectorObservationB);
        SimpleMatrix subtractionMatrix = vectorObservationB.minus(vectorObservationA);
        double distance = subtractionMatrix.normF();
        return distance;
    };

    private static final double MEAN_THRESHOLD = 0.01;

    static final Comparator<double[]> MeanComparator = (meanA, meanB) -> {
        if (meanA.length != meanB.length) {
            throw new IllegalArgumentException(String.format("Means are of different dimensions: %d, %d",
                                                             meanA.length, meanB.length));
        }
        SimpleMatrix meanVectorA = SimpleMatrix.wrap(DMatrixRMaj.wrap(1, meanA.length, meanA));
        SimpleMatrix meanVectorB = SimpleMatrix.wrap(DMatrixRMaj.wrap(1, meanB.length, meanB));
        double distance = meanVectorB.minus(meanVectorA).normF();
        if (distance < MEAN_THRESHOLD) {
            return 0;
        }
        return Double.compare(meanVectorA.normF(), meanVectorB.normF());
    };

    private static final Logger logger = LogManager.getLogger(ClusterAnalysis.class);

    public static List<Observation> generateData(int amount) {
        Random random = new Random();
        List<Observation> observations = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            double xValue = random.nextDouble(-1, 1);
            double yValue = random.nextDouble(-1, 1);
            Observation observation = new Observation(xValue, yValue);
            observations.add(observation);
        }
        return observations;
    }

    public static List<Cluster> cluster(List<Observation> observations, int clustersAmount) {
        try {
            logger.info("Cluster data: observations = {}, clusters = {}", observations.size(), clustersAmount);
            if (clustersAmount > observations.size()) {
                throw new IllegalArgumentException("Not enough observations to perform clustering: "
                                                   + observations.size());
            }
            Instant before = Instant.now();
            List<Cluster> clusters = observations
                    .stream()
                    .limit(clustersAmount)
                    .map((Observation mean) -> new Cluster(mean.values)).toList();
            int iteration = 0;
            boolean anyClusterMeanUpdated;
            do {
                logger.debug("Iteration #{}. Clustering...", iteration);
                iteration++;
                assignToClusters(observations, clusters);
                anyClusterMeanUpdated = updateMeans(clusters);
                String clustersSizes = clusters.stream()
                                               .mapToInt(cluster -> cluster.getObservations().size())
                                               .mapToObj(String::valueOf)
                                               .collect(Collectors.joining(", "));
                logger.debug("Clusters sizes: {}", clustersSizes);
            } while (anyClusterMeanUpdated);
            Instant after = Instant.now();
            Duration clusteringTime = Duration.between(before, after);
            logger.info("Clustering succeed in {}.{} sec", clusteringTime.getSeconds(), clusteringTime.toMillisPart());
            return clusters;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    private static void assignToClusters(List<Observation> observations, List<Cluster> clusters) {
        clusters.forEach(cluster -> cluster.observations.clear());
        observations.parallelStream().forEach(observation -> {
            Cluster relevantCluster = pickRelevantCluster(observation, clusters);
            relevantCluster.getObservations().add(observation);
        });
    }

    private static boolean updateMeans(List<Cluster> clusters) {
        boolean anyClusterMeanUpdated = clusters
                .parallelStream()
                .map(cluster -> {
                    double[] oldMean = cluster.mean;
                    double[] updatedMean = computeUpdatedMean(cluster);
                    boolean meanUpdated = MeanComparator.compare(updatedMean, oldMean) != 0;
                    if (meanUpdated) {
                        cluster.mean = updatedMean;
                        logger.trace("Cluster mean update: {} -> {} (delta: {})",
                                     oldMean, updatedMean,
                                     EuclideanDistance.compute(oldMean, updatedMean));
                    }
                    return meanUpdated;
                })
                .reduce(Boolean::logicalOr)
                .orElseThrow();
        return anyClusterMeanUpdated;
    }

    private static Cluster pickRelevantCluster(Observation observation, List<Cluster> clusters) {
        Cluster relevantCluster = clusters.parallelStream().min(Comparator.comparingDouble(
                                                  cluster -> EuclideanDistance.compute(observation.values, cluster.mean)))
                                          .orElseThrow();
        logger.trace("Observation {} relevant to a cluster with mean {} (distance: {})",
                     observation.values, relevantCluster.mean,
                     EuclideanDistance.compute(observation.values, relevantCluster.mean));
        return relevantCluster;
    }

    private static double[] computeUpdatedMean(Cluster cluster) {
        SimpleMatrix observationsSum = cluster.observations
                .parallelStream()
                .map(observation -> SimpleMatrix.wrap(
                        DMatrixRMaj.wrap(1, observation.getDimension(),
                                         observation.values)))
                .reduce(SimpleBase::plus)
                .orElseThrow();
        double[] updatedMean = observationsSum.divide(cluster.observations.size()).getDDRM().data;
        return updatedMean;
    }
}
