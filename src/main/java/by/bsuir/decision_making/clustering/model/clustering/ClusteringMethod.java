package by.bsuir.decision_making.clustering.model.clustering;

import java.util.function.Supplier;

public enum ClusteringMethod {
    K_MEANS(KMeansClustering::new),
    MAXIMIN(MaximinClustering::new);

    private final Supplier<ClusteringMethodAlgorithm> algorithmSupplier;

    ClusteringMethod(Supplier<ClusteringMethodAlgorithm> algorithmSupplier) {
        this.algorithmSupplier = algorithmSupplier;
    }

    public ClusteringMethodAlgorithm getAlgorithm() {
        return algorithmSupplier.get();
    }
}
