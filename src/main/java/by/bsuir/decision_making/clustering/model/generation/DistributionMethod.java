package by.bsuir.decision_making.clustering.model.generation;

public interface DistributionMethod {

    double generateValue();

    double getFirstValue();

    void setFirstValue(double value) throws IllegalArgumentException;

    double getSecondValue();

    void setSecondValue(double value) throws IllegalArgumentException, UnsupportedOperationException;
}
