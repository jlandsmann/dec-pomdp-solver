package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * {@inheritDoc}
 */
public class VectorCombinationBuilder<T> extends CombinationBuilder<Vector<T>, T> {

  /**
   * This method creates a stream of all combinations from the given list of lists.
   *
   * @param possibleValues a list of lists of items
   * @return a stream with all combinations
   */
  public static <T> Stream<Vector<T>> streamOf(List<? extends List<T>> possibleValues) {
    var builder = new VectorCombinationBuilder<T>();
    return builder.getStreamForEachCombination(possibleValues);
  }

  /**
   * This method creates a list of all combinations from the given list of lists.
   *
   * @param possibleValues a list of lists of items
   * @return a list with all combinations
   */
  public static <T> List<Vector<T>> listOf(List<? extends List<T>> possibleValues) {
    var builder = new VectorCombinationBuilder<T>();
    return builder.getListForEachCombination(possibleValues);
  }

  /**
   * This method creates a set of all combinations from the given list of lists.
   *
   * @param possibleValues a list of lists of items
   * @return a set with all combinations
   */
  public static <T> Set<Vector<T>> setOf(List<List<T>> possibleValues) {
    var builder = new VectorCombinationBuilder<T>();
    return builder.getSetForEachCombination(possibleValues);
  }

  /**
   * This method transforms the input list into a {@link Vector}
   * representing the combination.
   *
   * @param combinationAsList the combination as a list
   * @return a vector representing the combination
   */
  @Override
  protected Vector<T> transformListToCombination(List<T> combinationAsList) {
    return Vector.of(combinationAsList);
  }
}
