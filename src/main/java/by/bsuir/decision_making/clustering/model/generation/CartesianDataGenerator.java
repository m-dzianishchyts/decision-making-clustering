package by.bsuir.decision_making.clustering.model.generation;

import by.bsuir.decision_making.clustering.model.Observation;

public class CartesianDataGenerator implements DataGenerator {

    private final DistributionMethod firstCoordinateMethod;
    private final DistributionMethod secondCoordinateMethod;

    public CartesianDataGenerator(DistributionMethod firstCoordinateMethod, DistributionMethod secondCoordinateMethod) {
        this.firstCoordinateMethod = firstCoordinateMethod;
        this.secondCoordinateMethod = secondCoordinateMethod;
    }

    @Override
    public Observation generate() {
        double xValue = firstCoordinateMethod.generateValue();
        double yValue = secondCoordinateMethod.generateValue();
        Observation observation = new Observation(xValue, yValue);
        return observation;
    }
}
