package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class HistogramBuilder<T> {

  /**
   * This method creates a stream of all histograms with the list of possibleValue as the buckets
   *
   * @param possibleValues a list representing the buckets of the histogram
   * @param numberOfElements the total number of elements in the histogram
   * @return a stream with all histograms
   */
  public static <T> Stream<Histogram<T>> streamOf(Collection<T> possibleValues, int numberOfElements) {
    var builder = new HistogramBuilder<T>();
    return builder.of(possibleValues, numberOfElements).stream();
  }

  /**
   * This method creates a list of all histograms with the list of possibleValue as the buckets
   *
   * @param possibleValues a list representing the buckets of the histogram
   * @param numberOfElements the total number of elements in the histogram
   * @return a list with all histograms
   */
  public static <T> List<Histogram<T>> listOf(Collection<T> possibleValues, int numberOfElements) {
    var builder = new HistogramBuilder<T>();
    return builder.of(possibleValues, numberOfElements);
  }

  /**
   * This method creates a list of all peak-shaped histograms with the list of possibleValue as the buckets
   *
   * @param possibleValues a list representing the buckets of the histogram
   * @param numberOfElements the total number of elements in the histogram
   * @return a list with all peak-shaped histograms
   */
  public static <T> List<Histogram<T>> listOfPeakShaped(Collection<T> possibleValues, int numberOfElements) {
    var builder = new HistogramBuilder<T>();
    return builder.ofPeakShaped(possibleValues, numberOfElements);
  }

  public List<Histogram<T>> ofPeakShaped(Collection<T> possibleValues, int numberOfElements) {
    if (possibleValues.isEmpty()) return List.of();
    var totalHistograms = new ArrayList<Histogram<T>>();

    for (var bucket : possibleValues) {
      var histogram = Histogram.of(Map.of(bucket, numberOfElements));
      totalHistograms.add(histogram);
    }

    return totalHistograms;
  }

  public List<Histogram<T>> of(Collection<T> possibleValues, int numberOfElements) {
    return of(possibleValues, numberOfElements, 1);
  }

  public List<Histogram<T>> of(Collection<T> possibleValues, int numberOfElements, int stepSize) {
    if (possibleValues.isEmpty()) return List.of();
    var buckets = List.copyOf(possibleValues);
    var totalHistograms = new ArrayList<Histogram<T>>();

    var current = buckets.get(0);
    var child = buckets.subList(1, buckets.size());


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
