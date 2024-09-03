package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class DistributionCollector<K> implements Collector<Map.Entry<K, Double>, Map<K, Double>, Distribution<K>> {

  private final boolean normalize;

  public DistributionCollector() {
    this(false);
  }

  public DistributionCollector(boolean normalize) {
    this.normalize = normalize;
  }

  @Override
  public Supplier<Map<K, Double>> supplier() {
    return ConcurrentHashMap::new;
  }

  @Override
  public BiConsumer<Map<K, Double>, Map.Entry<K, Double>> accumulator() {
    return (mapping, entry) -> {
      var key = entry.getKey();
      var value = entry.getValue();
      var originalValue = mapping.getOrDefault(key, 0D);
      mapping.put(key, originalValue + value);
    };
  }

  @Override
  public BinaryOperator<Map<K, Double>> combiner() {
    return (mappingA, mappingB) -> {
      for (var entry : mappingB.entrySet()) {
        var key = entry.getKey();
        var value = entry.getValue();
        var originalValue = mappingA.getOrDefault(key, 0D);
        mappingA.put(key, originalValue + value);
      }
      return mappingA;
    };
  }

  @Override
  public Function<Map<K, Double>, Distribution<K>> finisher() {
    return normalize ? Distribution::normalizeOf : Distribution::of;
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Set.of(Characteristics.UNORDERED, Characteristics.CONCURRENT);
  }
}
