package by.bsuir.decision_making.clustering.model.generation;

import java.util.Random;

public class GaussianDistribution implements DistributionMethod {

    private final Random random = new Random();
    private double mean;
    private double deviation;

    @Override
    public double generateValue() {
        double nextValue = random.nextGaussian(mean, deviation);
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
    public void setSecondValue(double value) throws IllegalArgumentException {
        if (value < 0) {
            throw new IllegalArgumentException("Deviation cannot be negative");
        }
        deviation = value;
    }

    @Override
    public double getSecondValue() {
        return deviation;
    }
}
