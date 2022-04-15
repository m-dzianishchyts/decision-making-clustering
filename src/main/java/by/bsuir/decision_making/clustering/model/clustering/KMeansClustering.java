package by.bsuir.decision_making.clustering.model.clustering;

import by.bsuir.decision_making.clustering.model.Cluster;
import by.bsuir.decision_making.clustering.model.Observation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ejml.data.DMatrixRMaj;
import org.ejml.simple.SimpleBase;
import org.ejml.simple.SimpleMatrix;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class KMeansClustering implements ClusteringMethodAlgorithm {

    private static final Logger logger = LogManager.getLogger(KMeansClustering.class);

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

    private ClusteringConfig config = ClusteringConfig.BLANK_CONFIG;

    @Override
    public List<Cluster> cluster(List<Observation> observations) {
        try {
            int clustersAmount = config.getProperty(ClusteringConfig.Property.CLUSTERS_AMOUNT);
            logger.info("Cluster data: observations = {}, clusters = {}", observations.size(), clustersAmount);
            if (clustersAmount > observations.size()) {
                String message = String.format("Not enough observations to perform clustering: %d. Needed: %d",
                                               observations.size(), clustersAmount);
                throw new IllegalArgumentException(message);
            }

            Instant before = Instant.now();
            List<Cluster> clusters = observations
                    .stream()
                    .limit(clustersAmount)
                    .map((Observation mean) -> new Cluster(mean.getValues())).toList();
            int iteration = 0;
            boolean anyClusterMeanUpdated;

            do {
                logger.debug("Iteration #{}. Clustering...", iteration);
                assignToClusters(observations, clusters);
                anyClusterMeanUpdated = updateMeans(clusters);
                logger.debug("Clusters sizes: {}", String.join(", ", clusters
                        .stream()
                        .map(cluster -> cluster.getObservations().size())
                        .map(Object::toString).toList()));
                iteration++;
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

    @Override
    public ClusteringConfig getClusteringConfig() {
        return config;
    }

    @Override
    public void setClusteringConfig(ClusteringConfig config) {
        this.config = config;
    }

    private static void assignToClusters(List<Observation> observations, List<Cluster> clusters) {
        clusters.forEach(cluster -> cluster.getObservations().clear());
        observations.parallelStream().forEach(observation -> {
            Cluster relevantCluster = pickRelevantCluster(observation, clusters);
            relevantCluster.getObservations().add(observation);
        });
    }

    private static boolean updateMeans(List<Cluster> clusters) {
        boolean anyClusterMeanUpdated = clusters
                .parallelStream()
                .map(cluster -> {
                    double[] oldMean = cluster.getMean();
                    double[] updatedMean = computeUpdatedMean(cluster);
                    boolean meanUpdated = MeanComparator.compare(updatedMean, oldMean) != 0;
                    if (meanUpdated) {
                        cluster.setMean(updatedMean);
                        logger.trace("Cluster mean update: {} -> {} (delta: {})", oldMean, updatedMean,
                                     EuclideanDistance.compute(oldMean, updatedMean));
                    }
                    return meanUpdated;
                })
                .reduce(Boolean::logicalOr)
                .orElseThrow();
        return anyClusterMeanUpdated;
    }

    private static Cluster pickRelevantCluster(Observation observation, List<Cluster> clusters) {
        Optional<Cluster> emptyCluster = clusters
                .stream()
                .filter(cluster -> cluster.getObservations().isEmpty())
                .findAny();
        Cluster relevantCluster = emptyCluster.orElseGet(() -> clusters
                .stream()
                .min(Comparator.comparingDouble(cluster -> EuclideanDistance.compute(observation.getValues(),
                                                                                     cluster.getMean())))
                .orElseThrow()
        );
        logger.trace("Observation {} relevant to a cluster with mean {} (distance: {})",
                     observation.getValues(), relevantCluster.getMean(),
                     EuclideanDistance.compute(observation.getValues(), relevantCluster.getMean()));
        return relevantCluster;
    }

    private static double[] computeUpdatedMean(Cluster cluster) {
        SimpleMatrix observationsSum = cluster
                .getObservations()
                .parallelStream()
                .map(observation -> SimpleMatrix.wrap(DMatrixRMaj.wrap(1, observation.getDimension(),
                                                                       observation.getValues())))
                .reduce(SimpleBase::plus)
                .orElseThrow();
        double[] updatedMean = observationsSum.divide(cluster.getObservations().size()).getDDRM().data;
        return updatedMean;
    }
}
