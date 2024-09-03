package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.*;
import java.util.stream.IntStream;

public class Histogram<T> {

  private final Map<T, Integer> buckets;

  public static <U> Histogram<U> from(Collection<U> collection) {
    return collection.stream().collect(CustomCollectors.toHistogram());
  }

  public static <U> Histogram<U> from(Vector<U> vector) {
    return vector.stream().collect(CustomCollectors.toHistogram());
  }

  public static <U> Histogram<U> of(Map<U, Integer> buckets) {
    return new Histogram<>(buckets);
  }

  public Histogram(Map<T, Integer> buckets) {
    this.buckets = new HashMap<>(buckets);
  }

  public Histogram(int numberOfBuckets) {
    this.buckets = new HashMap<>(numberOfBuckets, 1);
  }

  public Histogram() {
    this.buckets = new HashMap<>();
  }

  public int get(T element) {
    return buckets.getOrDefault(element, 0);
  }

  public void add(T element, int numberOfElements) {
    var currentNumberOfElements = get(element);
    put(element, currentNumberOfElements + numberOfElements);
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

  public void merge(Histogram<T> other) {
    for (var entry : other.entrySet()) {
      add(entry.getKey(), entry.getValue());
    }
  }

  public Set<Map.Entry<T, Integer>> entrySet() {
    return buckets.entrySet();
  }

  public Set<T> keySet() {
    return buckets.keySet();
  }

  public List<T> toList() {
    return buckets.keySet()
      .stream()
      .sorted(Comparator.comparing(Objects::toString))
      .flatMap(element -> IntStream.range(0, get(element)).mapToObj(i -> element))
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
