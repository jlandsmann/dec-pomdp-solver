package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Histogram<T> {

  private final Map<T, Integer> buckets;

  public static <U> Histogram<U> of(Map<U, Integer> buckets) {
    return new Histogram<>(buckets);
  }

  public Histogram(Map<T, Integer> buckets) {
    this.buckets = new HashMap<>(buckets);
  }

  public Histogram(int numberOfBuckets) {
    this.buckets = new HashMap<>(numberOfBuckets, 1);
  }

  public int get(T element) {
    return buckets.getOrDefault(element, 0);
  }

  public void put(T element, int numberOfElements) {
    buckets.put(element, numberOfElements);
  }

  public int numberOfElements() {
    return buckets.values().stream().mapToInt(v -> v).sum();
  }

  public int numberOfBuckets() {
    return buckets.size();
  }

  public List<T> toList() {
    return buckets.keySet()
      .stream()
      .flatMap(element -> IntStream.of(get(element)).mapToObj(i -> element))
      .toList();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    return buckets.equals(((Histogram<?>) obj).buckets);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (var key : buckets.keySet()) {
      var count = buckets.get(key);
      sb.append(key).append("(").append(count).append(")");
      sb.append(", ");
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash("Histogram", buckets);
  }
}
