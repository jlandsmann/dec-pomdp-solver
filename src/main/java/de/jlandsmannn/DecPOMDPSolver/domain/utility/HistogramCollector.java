package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class HistogramCollector<T> implements Collector<T, Histogram<T>, Histogram<T>> {
  @Override
  public Supplier<Histogram<T>> supplier() {
    return Histogram::new;
  }

  @Override
  public BiConsumer<Histogram<T>, T> accumulator() {
    return ((tHistogram, t) -> tHistogram.add(t, 1));
  }

  @Override
  public BinaryOperator<Histogram<T>> combiner() {
    return ((tHistogram, tHistogram2) -> {
      tHistogram.merge(tHistogram);
      return tHistogram;
    });
  }

  @Override
  public Function<Histogram<T>, Histogram<T>> finisher() {
    return Function.identity();
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Set.of(Characteristics.UNORDERED, Characteristics.IDENTITY_FINISH);
  }
}
