package by.bsuir.decision_making.clustering.model.clustering;

import by.bsuir.decision_making.clustering.model.Cluster;
import by.bsuir.decision_making.clustering.model.DistanceMethod;
import by.bsuir.decision_making.clustering.model.Observation;
import org.ejml.data.DMatrixRMaj;
import org.ejml.simple.SimpleMatrix;

import java.util.List;

public interface ClusteringMethodAlgorithm {

    DistanceMethod<double[]> EuclideanDistance = (valuesA, valuesB) -> {
        if (valuesA.length != valuesB.length) {
            throw new IllegalArgumentException(String.format("Values are of different dimensions: %d, %d",
                                                             valuesA.length, valuesB.length));
        }
        DMatrixRMaj rawVectorObservationA = DMatrixRMaj.wrap(1, valuesA.length, valuesA);
        DMatrixRMaj rawVectorObservationB = DMatrixRMaj.wrap(1, valuesB.length, valuesB);
        SimpleMatrix vectorObservationA = SimpleMatrix.wrap(rawVectorObservationA);
        SimpleMatrix vectorObservationB = SimpleMatrix.wrap(rawVectorObservationB);
        SimpleMatrix subtractionMatrix = vectorObservationB.minus(vectorObservationA);
        double distance = subtractionMatrix.normF();
        return distance;
    };

    List<Cluster> cluster(List<Observation> observations);

    ClusteringConfig getClusteringConfig();

    void setClusteringConfig(ClusteringConfig config);
}
