package de.jlandsmannn.DecPOMDPSolver.domain.utility.tuple;

import java.util.Objects;
import java.util.function.Function;

public class Tuple2<A, B> implements Function<Integer, Object> {
  private final A a;
  private final B b;

  protected Tuple2(A a, B b) {
    this.a = a;
    this.b = b;
  }

  public A first() {
    return a;
  }

  public B second() {
    return b;
  }

  public Object get(int i) {
    return switch (i) {
      case 0 -> a;
      case 1 -> b;
      default -> throw new IndexOutOfBoundsException();
    };
  }

  @Override
  public Object apply(Integer integer) {
    return get(integer);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Tuple2<?, ?> tuple2)) return false;
    return Objects.equals(a, tuple2.a) && Objects.equals(b, tuple2.b);
  }

  @Override
  public int hashCode() {
    return Objects.hash(a, b);
  }

  public int size() {
    return 2;
  }
}
