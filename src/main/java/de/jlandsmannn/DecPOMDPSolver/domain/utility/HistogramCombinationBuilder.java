package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class HistogramCombinationBuilder<T> extends CombinationBuilder<Map.Entry<Histogram<T>, Double>, Map.Entry<Histogram<T>, Double>> {
  
  /**
   * This method creates a stream of all combinations from the given list of lists.
   *
   * @param possibleValues a list of lists of items
   * @return a stream with all combinations
   */
  public static <T> Stream<Map.Entry<Histogram<T>, Double>> streamOf(List<? extends List<Map.Entry<Histogram<T>, Double>>> possibleValues) {
    var builder = new HistogramCombinationBuilder<T>();
    return builder.getStreamForEachCombination(possibleValues);
  }

  /**
   * This method creates a list of all combinations from the given list of lists.
   *
   * @param possibleValues a list of lists of histograms
   * @return a list with all combinations
   */
  public static <T> List<Map.Entry<Histogram<T>, Double>> listOf(List<? extends List<Map.Entry<Histogram<T>, Double>>> possibleValues) {
    var builder = new HistogramCombinationBuilder<T>();
    return builder.getListForEachCombination(possibleValues);
  }

  /**
   * This method creates a set of all combinations from the given list of lists.
   *
   * @param possibleValues a list of lists of items
   * @return a set with all combinations
   */
  public static <T> Set<Map.Entry<Histogram<T>, Double>> setOf(List<? extends List<Map.Entry<Histogram<T>, Double>>> possibleValues) {
    var builder = new HistogramCombinationBuilder<T>();
    return builder.getSetForEachCombination(possibleValues);
  }

  @Override
  protected Map.Entry<Histogram<T>, Double> transformListToCombination(List<Map.Entry<Histogram<T>, Double>> combinationAsList) {
    var histogram = new Histogram<T>(combinationAsList.size());
    var probability = 1D;
    for (Map.Entry<Histogram<T>, Double> partialHistogram : combinationAsList) {
      for (T bucket : partialHistogram.getKey().keySet()) {
        histogram.add(bucket, partialHistogram.getKey().get(bucket));
      }
      probability *= partialHistogram.getValue();
    }
    return Map.entry(histogram, probability);
  }
}
