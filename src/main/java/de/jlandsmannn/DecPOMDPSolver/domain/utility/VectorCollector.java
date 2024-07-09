package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class VectorCollector<T> implements Collector<T, List<T>, Vector<T>> {
  @Override
  public Supplier<List<T>> supplier() {
    return ArrayList::new;
  }

  @Override
  public BiConsumer<List<T>, T> accumulator() {
    return List::add;
  }

  @Override
  public BinaryOperator<List<T>> combiner() {
    return (listA, listB) -> {
      listA.addAll(listB);
      return listA;
    };
  }

  @Override
  public Function<List<T>, Vector<T>> finisher() {
    return Vector::new;
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Set.of();
  }
}
