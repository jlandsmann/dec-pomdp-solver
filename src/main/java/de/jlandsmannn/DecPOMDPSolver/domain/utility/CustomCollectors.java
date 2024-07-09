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
}
