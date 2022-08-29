package by.bsuir.decision_making.clustering.model.generation;

import by.bsuir.decision_making.clustering.model.Observation;

import java.util.List;
import java.util.stream.Stream;

public interface DataGenerator {

    default List<Observation> generateData(int amount) {
        List<Observation> observations = Stream.generate(this::generate).limit(amount).toList();
        return observations;
    }

    Observation generate();
}
