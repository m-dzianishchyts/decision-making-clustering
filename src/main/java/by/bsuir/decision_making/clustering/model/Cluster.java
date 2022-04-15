package by.bsuir.decision_making.clustering.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class Cluster {

    double[] mean;
    List<Observation> observations;
    private String name;

    public Cluster(double[] mean) {
        this(mean, null);
    }

    private Cluster(double[] mean, String name) {
        Objects.requireNonNull(mean);
        this.mean = mean;
        this.name = name;
        this.observations = Collections.synchronizedList(new ArrayList<>());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double[] getMean() {
        return mean;
    }

    public void setMean(double[] mean) {
        Objects.requireNonNull(mean);
        if (this.mean.length != mean.length) {
            throw new IllegalArgumentException("Illegal mean dimension:" + mean.length);
        }
        this.mean = mean;
    }

    public List<Observation> getObservations() {
        return observations;
    }

    public void setObservations(List<Observation> observations) {
        Objects.requireNonNull(observations);
        this.observations = Collections.synchronizedList(new ArrayList<>(observations));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, Arrays.hashCode(mean), observations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Cluster cluster = (Cluster) o;
        return Objects.equals(name, cluster.name)
               && Arrays.equals(mean, cluster.mean)
               && observations.equals(cluster.observations);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Cluster.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("mean=" + Arrays.toString(mean))
                .add("observations=" + observations)
                .toString();
    }
}
