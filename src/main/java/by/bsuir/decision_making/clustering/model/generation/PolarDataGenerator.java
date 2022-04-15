package by.bsuir.decision_making.clustering.model.generation;

import by.bsuir.decision_making.clustering.model.Observation;

public class PolarDataGenerator implements DataGenerator {

    private final DistributionMethod firstCoordinateMethod;
    private final DistributionMethod secondCoordinateMethod;

    public PolarDataGenerator(DistributionMethod firstCoordinateMethod,
                              DistributionMethod secondCoordinateMethod) {
        this.firstCoordinateMethod = firstCoordinateMethod;
        this.secondCoordinateMethod = secondCoordinateMethod;
    }

    @Override
    public Observation generate() {
        double angle = firstCoordinateMethod.generateValue();
        double radius = Math.max(secondCoordinateMethod.generateValue(), 0);
        double xValue = radius * Math.cos(angle);
        double yValue = radius * Math.sin(angle);
        Observation observation = new Observation(xValue, yValue);
        return observation;
    }
}