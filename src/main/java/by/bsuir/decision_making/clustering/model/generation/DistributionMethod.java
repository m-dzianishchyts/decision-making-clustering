package by.bsuir.decision_making.clustering.model.generation;

public interface DistributionMethod {

    double generateValue();

    void setFirstValue(double value) throws IllegalArgumentException;
    double getFirstValue();

    void setSecondValue(double value) throws IllegalArgumentException, UnsupportedOperationException;
    double getSecondValue();
}
