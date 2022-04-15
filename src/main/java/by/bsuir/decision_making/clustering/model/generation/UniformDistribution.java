package by.bsuir.decision_making.clustering.model.generation;

import java.util.Random;

public class UniformDistribution implements DistributionMethod {

    private final Random random = new Random();
    private double bound;

    @Override
    public double generateValue() {
        double nextValue = random.nextDouble(bound);
        return nextValue;
    }

    @Override
    public double getFirstValue() {
        return bound;
    }

    @Override
    public void setFirstValue(double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        }
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Bound must be finite");
        }
        this.bound = value;
    }

    @Override
    public double getSecondValue() {
        throw new UnsupportedOperationException("No second value for uniform distribution");
    }

    @Override
    public void setSecondValue(double value) {
        throw new UnsupportedOperationException("No second value for uniform distribution");
    }
}
