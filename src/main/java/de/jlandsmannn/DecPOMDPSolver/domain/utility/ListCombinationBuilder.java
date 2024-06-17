package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.List;
import java.util.stream.Stream;

/**
 * {@inheritDoc}
 */
public class ListCombinationBuilder<T> extends CombinationBuilder<List<T>, T> {

  /**
   * This method creates a stream of all combinations from the given list of lists.
   *
   * @param possibleValues a list of lists of items
   * @return a stream with all combinations
   */
  public static <T> Stream<List<T>> streamOf(List<? extends List<T>> possibleValues) {
    var builder = new ListCombinationBuilder<T>();
    return builder.getStreamForEachCombination(possibleValues);
  }

  /**
   * This method creates a list of all combinations from the given list of lists.
   *
   * @param possibleValues a list of lists of items
   * @return a list with all combinations
   */
  public static <T> List<List<T>> listOf(List<? extends List<T>> possibleValues) {
    var builder = new ListCombinationBuilder<T>();
    return builder.getListForEachCombination(possibleValues);
  }

  /**
   * This method transforms the input list into a readonly list.
   *
   * @param combinationAsList the combination as a list
   * @return a readonly list representing the combination
   */
  @Override
  protected List<T> transformListToCombination(List<T> combinationAsList) {
    return List.copyOf(combinationAsList);
  }
}
