package by.bsuir.decision_making.clustering.model.clustering;

public class ClusteringConfig {

    public static final int DEFAULT_CLUSTERS_AMOUNT = 1;

    public static final ClusteringConfig BLANK_CONFIG = new ClusteringConfig(DEFAULT_CLUSTERS_AMOUNT);

    private int clustersAmount;

    public ClusteringConfig() {
    }

    private ClusteringConfig(int clustersAmount) {
        if (clustersAmount <= 0) {
            throw new IllegalArgumentException("Clusters amount must be positive");
        }
        this.clustersAmount = clustersAmount;
    }

    public void setProperty(Property property, int value) {
        switch (property) {
            case CLUSTERS_AMOUNT -> clustersAmount = value;
        }
    }

    public int getProperty(Property property) {
        int value = switch (property) {
            case CLUSTERS_AMOUNT -> clustersAmount;
        };
        return value;
    }

    public enum Property {
        CLUSTERS_AMOUNT
    }
}
