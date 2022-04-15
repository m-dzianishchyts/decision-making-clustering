package by.bsuir.decision_making.clustering.model;

import by.bsuir.decision_making.clustering.model.generation.CartesianDataGenerator;
import by.bsuir.decision_making.clustering.model.generation.DataGenerator;
import by.bsuir.decision_making.clustering.model.generation.DistributionMethod;
import by.bsuir.decision_making.clustering.model.generation.PolarDataGenerator;

import java.util.function.BiFunction;

public enum CoordinateSystem {
    CARTESIAN("X", "Y", CartesianDataGenerator::new),
    POLAR("Phi", "R", PolarDataGenerator::new);

    private final String firstCoordinateName;
    private final String secondCoordinateName;
    private final BiFunction<DistributionMethod, DistributionMethod, DataGenerator> dataGeneratorSupplier;

    CoordinateSystem(String firstCoordinateName, String secondCoordinateName,
                     BiFunction<DistributionMethod, DistributionMethod, DataGenerator> dataGeneratorSupplier) {
        this.firstCoordinateName = firstCoordinateName;
        this.secondCoordinateName = secondCoordinateName;
        this.dataGeneratorSupplier = dataGeneratorSupplier;
    }

    public String getFirstCoordinateName() {
        return firstCoordinateName;
    }

    public String getSecondCoordinateName() {
        return secondCoordinateName;
    }

    public DataGenerator getDataGenerator(DistributionMethod firstCoordinateMethod,
                                          DistributionMethod secondCoordinateMethod) {
        DataGenerator dataGenerator = dataGeneratorSupplier.apply(firstCoordinateMethod, secondCoordinateMethod);
        return dataGenerator;
    }
}
