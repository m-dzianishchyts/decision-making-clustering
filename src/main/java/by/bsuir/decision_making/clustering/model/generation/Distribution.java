package by.bsuir.decision_making.clustering.model.generation;

import java.util.function.Supplier;

public enum Distribution {

    UNIFORM("Bound", null, UniformDistribution::new),
    GAUSSIAN("Mean", "Deviation", GaussianDistribution::new),
    EXPONENTIAL("Mean", null, ExponentialDistribution::new);

    private final String firstValueName;
    private final String secondValueName;
    private final Supplier<DistributionMethod> algorithmSupplier;

    Distribution(String firstValueName, String secondValueName,
                 Supplier<DistributionMethod> algorithmSupplier) {
        this.firstValueName = firstValueName;
        this.secondValueName = secondValueName;
        this.algorithmSupplier = algorithmSupplier;
    }

    public String getFirstValueName() {
        return firstValueName;
    }

    public String getSecondValueName() {
        return secondValueName;
    }

    public DistributionMethod getAlgorithm() {
        return algorithmSupplier.get();
    }
}
