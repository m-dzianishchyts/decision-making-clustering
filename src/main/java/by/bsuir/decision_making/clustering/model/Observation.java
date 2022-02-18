package by.bsuir.decision_making.clustering.model;

import java.util.Arrays;
import java.util.StringJoiner;

public class Observation {

    final double[] values;

    public Observation(double... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Values cannot be null or empty");
        }
        this.values = Arrays.copyOf(values, values.length);
    }

    public Observation(Observation observation) {
        double[] otherValues = observation.values;
        values = Arrays.copyOf(otherValues, otherValues.length);
    }

    public double getValue(int index) {
        return values[index];
    }

    public int getDimension() {
        return values.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (Observation) o;
        return Arrays.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Observation.class.getSimpleName() + "[", "]")
                .add("values=" + Arrays.toString(values))
                .toString();
    }
}
