package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CustomCollectors {
  public static <T> Collector<T, ?, Histogram<T>> toHistogram() {
    return new HistogramCollector<T>();
  }

  public static <T> Collector<T, ?, Vector<T>> toVector() {
    return new VectorCollector<T>();
  }

  public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> toMap() {
    return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
  }

  public static <K> Collector<Map.Entry<K, Double>, ?, Distribution<K>> toDistribution() {
    return new DistributionCollector<>();
  }

  public static <K> Collector<Map.Entry<K, Double>, ?, Distribution<K>> toFilledUpDistribution(K extraElement) {
    return new DistributionCollector<>(extraElement);
  }

  public static <K> Collector<Map.Entry<K, Double>, ?, Distribution<K>> toNormalizedDistribution() {
    return new DistributionCollector<>(true);
  }
}
