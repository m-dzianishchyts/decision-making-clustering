package by.bsuir.decision_making.clustering.model.clustering;

public class ClusteringConfig {

    public static final int DEFAULT_CLUSTERS_AMOUNT = 1;

    public static final ClusteringConfig BLANK_CONFIG = new ClusteringConfig(DEFAULT_CLUSTERS_AMOUNT);

    private final int clustersAmount;

    public ClusteringConfig(int clustersAmount) {
        if (clustersAmount <= 0) {
            throw new IllegalArgumentException("Clusters amount must be positive");
        }
        this.clustersAmount = clustersAmount;
    }

    public int getClustersAmount() {
        return clustersAmount;
    }
}
