package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class HistogramBuilder<T> {

  public static <T> Stream<Histogram<T>> streamOf(List<T> possibleValues, int numberOfElements) {
    var builder = new HistogramBuilder<T>();
    return builder.of(possibleValues, numberOfElements).stream();
  }

  /**
   * This method creates a list of all combinations from the given list of lists.
   *
   * @param possibleValues a list of lists of items
   * @return a list with all combinations
   */
  public static <T> List<Histogram<T>> listOf(List<T> possibleValues, int numberOfElements) {
    var builder = new HistogramBuilder<T>();
    return builder.of(possibleValues, numberOfElements);
  }


  public List<Histogram<T>> of(List<T> possibleValues, int numberOfElements) {
    return of(possibleValues, numberOfElements, 1);
  }

  public List<Histogram<T>> of(List<T> possibleValues, int numberOfElements, int stepSize) {
    if (possibleValues.isEmpty()) return List.of();
    var totalHistograms = new ArrayList<Histogram<T>>();

    var current = possibleValues.get(0);
    var child = possibleValues.subList(1, possibleValues.size());


    var histogram = Histogram.of(Map.of(current, numberOfElements));
    totalHistograms.add(histogram);

    for (int i = numberOfElements - stepSize; i > 0; i -= stepSize) {
      var childNumberOfElements = numberOfElements - i;
      var histograms = of(child, childNumberOfElements, stepSize);
      int finalI = i;
      histograms.forEach(h -> h.put(current, finalI));
      totalHistograms.addAll(histograms);
    }

    var histograms = of(child, numberOfElements, stepSize);
    totalHistograms.addAll(histograms);

    return totalHistograms;
  }
}
