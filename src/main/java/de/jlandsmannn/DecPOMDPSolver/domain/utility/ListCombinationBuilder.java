package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.List;
import java.util.stream.Stream;

public class ListCombinationBuilder<T> extends CombinationBuilder<List<T>, T> {

  public static <T> Stream<List<T>> streamOf(List<? extends List<T>> possibleValues) {
    var builder = new ListCombinationBuilder<T>();
    return builder.getStreamForEachCombination(possibleValues);
  }

  public static <T> List<List<T>> listOf(List<? extends List<T>> possibleValues) {
    var builder = new ListCombinationBuilder<T>();
    return builder.getListForEachCombination(possibleValues);
  }

  @Override
  protected List<T> transformListToCombination(List<T> combinationAsList) {
    return List.copyOf(combinationAsList);
  }
}
