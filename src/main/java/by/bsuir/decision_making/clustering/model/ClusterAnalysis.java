package by.bsuir.decision_making.clustering.model;

import java.util.ArrayList;
import java.util.List;

public class ClusterAnalysis {

    public static List<Observation> generateData(int amount) {
        List<Observation> observations = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            double xValue = generateRandomDouble(-1, 1);
            double yValue = generateRandomDouble(-1, 1);
            Observation observation = new Observation(xValue, yValue);
            observations.add(observation);
        }
        return observations;
    }

    private static double generateRandomDouble(double from, double to) {
        if (to < from) {
            throw new IllegalArgumentException(String.format("Invalid range (%f, %f)", from, to));
        }
        double value = (to - from) * Math.random() + from;
        return value;
    }
}
