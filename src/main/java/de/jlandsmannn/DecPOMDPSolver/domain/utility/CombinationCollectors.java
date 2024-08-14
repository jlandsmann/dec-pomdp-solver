package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class CombinationCollectors {
  public static <T> Collector<List<T>, ?, Stream<Vector<T>>> toCombinationVectors() {
    return new VectorCombinationBuilder<T>();
  }
  public static <T> Collector<List<T>, ?, Stream<List<T>>> toCombinationLists() {
    return new ListCombinationBuilder<T>();
  }
}
