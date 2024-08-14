package de.jlandsmannn.DecPOMDPSolver.domain.utility.tuple;

import java.util.Objects;

public class Tuple3<A, B, C> extends Tuple2<A, B> {
  private final C c;

  protected Tuple3(A a, B b, C c) {
    super(a, b);
    this.c = c;
  }

  public C third() {
    return c;
  }

  @Override
  public Object get(int i) {
    if (i == 2) return c;
    return super.get(i);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Tuple3<?, ?, ?> tuple3)) return false;
    if (!super.equals(o)) return false;
    return Objects.equals(c, tuple3.c);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), c);
  }
}
