package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class VectorCombinationBuilder<T> extends CombinationBuilder<Vector<T>, T> {

  public static <T> Stream<Vector<T>> streamOf(List<? extends List<T>> possibleValues) {
    var builder = new VectorCombinationBuilder<T>();
    return builder.getStreamForEachCombination(possibleValues);
  }

  public static <T> List<Vector<T>> listOf(List<? extends List<T>> possibleValues) {
    var builder = new VectorCombinationBuilder<T>();
    return builder.getListForEachCombination(possibleValues);
  }

  VectorCombinationBuilder() {
    super();
  }

  public static <T> Set<Vector<T>> setOf(List<List<T>> possibleValues) {
    var builder = new VectorCombinationBuilder<T>();
    return builder.getSetForEachCombination(possibleValues);
  }

  @Override
  protected Vector<T> transformListToCombination(List<T> combinationAsList) {
    return Vector.of(combinationAsList);
  }
}
