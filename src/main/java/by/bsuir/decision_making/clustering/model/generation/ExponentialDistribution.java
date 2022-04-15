package by.bsuir.decision_making.clustering.model.generation;

import java.util.Random;

public class ExponentialDistribution implements DistributionMethod {

    private final Random random = new Random();
    private double mean;

    @Override
    public double generateValue() {
        double nextValue = random.nextExponential() * mean;
        return nextValue;
    }

    @Override
    public void setFirstValue(double value) {
        mean = value;
    }

    @Override
    public double getFirstValue() {
        return mean;
    }

    @Override
    public void setSecondValue(double value) {
        throw new UnsupportedOperationException("No second value for exponential distribution");
    }

    @Override
    public double getSecondValue() {
        throw new UnsupportedOperationException("No second value for exponential distribution");
    }
}
