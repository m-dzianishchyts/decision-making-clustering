package by.bsuir.decision_making.clustering.model.clustering;

import by.bsuir.decision_making.clustering.model.Cluster;
import by.bsuir.decision_making.clustering.model.Observation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MaximinClustering implements ClusteringMethodAlgorithm {

    private static final Logger logger = LogManager.getLogger(MaximinClustering.class);

    private ClusteringConfig config;

    @Override
    public List<Cluster> cluster(List<Observation> observations) {
        try {
            logger.info("Cluster data: observations = {}", observations.size());
            Instant before = Instant.now();

            Cluster firstCluster = defineFirstCluster(observations);
            List<Cluster> clusters = clusterIncrementally(observations, List.of(firstCluster));

            Instant after = Instant.now();
            Duration clusteringTime = Duration.between(before, after);
            logger.info("Clustering succeed in {}.{} sec", clusteringTime.getSeconds(), clusteringTime.toMillisPart());
            return clusters;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    private Cluster defineFirstCluster(List<Observation> observations) {
        double[] origin = new double[observations.get(0).getDimension()];
        Observation mean = observations
                .stream()
                .max(Comparator.comparingDouble(value -> ClusteringMethodAlgorithm.EuclideanDistance
                        .compute(value.getValues(), origin)))
                .orElseThrow();
        Cluster firstCluster = new Cluster(mean.getValues());
        firstCluster.getObservations().addAll(observations);
        return firstCluster;
    }

    private List<Cluster> clusterIncrementally(List<Observation> observations, List<Cluster> clusters) {
        List<Cluster> resultClusters = new ArrayList<>(clusters);
        int iteration = 0;
        boolean thresholdReached;
        do {
            logger.debug("Iteration #{}. Clusters: {}. Keep clustering...", resultClusters.size(), iteration);
            Map.Entry<Observation, Double> farthestObservationEntry = resultClusters
                    .parallelStream()
                    .map(findFarObservations())
                    .max(Comparator.comparingDouble(Map.Entry::getValue))
                    .orElseThrow();
            Observation farthestObservation = farthestObservationEntry.getKey();
            double maxDistanceToFarthestObservation = farthestObservationEntry.getValue();
            double threshold = computeThreshold(resultClusters);
            thresholdReached = maxDistanceToFarthestObservation > threshold;
            if (thresholdReached) {
                Cluster cluster = new Cluster(farthestObservation.getValues());
                resultClusters.add(cluster);
                redistributeObservations(observations, resultClusters);
            }
            logger.debug("Clusters sizes: {}", String.join(", ", resultClusters
                    .stream()
                    .map(cluster -> cluster.getObservations().size())
                    .map(Object::toString).toList()));
            iteration++;
        } while (thresholdReached);
        return resultClusters;
    }

    private Function<Cluster, Map.Entry<Observation, Double>> findFarObservations() {
        return cluster -> {
            Map.Entry<Observation, Double> farObservationWithDistance = cluster
                    .getObservations()
                    .parallelStream()
                    .max(Comparator.comparingDouble(o -> EuclideanDistance.compute(o.getValues(), cluster.getMean())))
                    .map(far -> Map.entry(far, EuclideanDistance.compute(far.getValues(), cluster.getMean())))
                    .orElseThrow();
            return farObservationWithDistance;
        };
    }

    private double computeThreshold(List<Cluster> clusters) {
        List<Cluster> clustersA = Collections.unmodifiableList(clusters);
        List<Cluster> clustersB = Collections.unmodifiableList(clusters);
        double averageDistance = clustersA
                .stream()
                .map(Cluster::getMean)
                .flatMap(meanA -> clustersB
                        .stream()
                        .map(Cluster::getMean).map(meanB -> EuclideanDistance.compute(meanA, meanB)))
                .mapToDouble(Double::doubleValue)
                .average()
                .orElseThrow();
        double threshold = averageDistance / 2;
        return threshold;
    }

    private void redistributeObservations(List<Observation> observations, List<Cluster> clusters) {
        clusters.forEach(cluster -> cluster.getObservations().clear());
        observations.parallelStream().forEach(observation -> {
            Cluster relevantCluster = pickRelevantCluster(observation, clusters);
            relevantCluster.getObservations().add(observation);
        });
    }

    private static Cluster pickRelevantCluster(Observation observation, List<Cluster> clusters) {
        Cluster relevantCluster = clusters
                .stream()
                .min(Comparator.comparingDouble(cluster -> EuclideanDistance.compute(observation.getValues(),
                                                                                     cluster.getMean())))
                .orElseThrow();
        logger.trace("Observation {} relevant to a cluster with mean {} (distance: {})",
                     observation.getValues(), relevantCluster.getMean(),
                     EuclideanDistance.compute(observation.getValues(), relevantCluster.getMean()));
        return relevantCluster;
    }

    @Override
    public ClusteringConfig getClusteringConfig() {
        return config;
    }

    @Override
    public void setClusteringConfig(ClusteringConfig config) {
        this.config = config;
    }
}
