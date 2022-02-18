package by.bsuir.decision_making.clustering.model;

@FunctionalInterface
public interface DistanceMethod<T> {

    double compute(T instanceA, T instanceB);
}
