package by.bsuir.decision_making.clustering.model.clustering;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

public enum ClusteringMethod {
    K_MEANS(KMeansClustering::new, Set.of(ClusteringConfig.Property.CLUSTERS_AMOUNT)),
    MAXIMIN(MaximinClustering::new, Collections.EMPTY_SET);

    private final Supplier<ClusteringMethodAlgorithm> algorithmSupplier;
    private final Set<ClusteringConfig.Property> supportedProperties;

    ClusteringMethod(Supplier<ClusteringMethodAlgorithm> algorithmSupplier,
                     Set<ClusteringConfig.Property> supportedProperties) {
        this.algorithmSupplier = algorithmSupplier;
        this.supportedProperties = supportedProperties;
    }

    public Set<ClusteringConfig.Property> getSupportedProperties() {
        return supportedProperties;
    }

    public ClusteringMethodAlgorithm getAlgorithm() {
        return algorithmSupplier.get();
    }
}
