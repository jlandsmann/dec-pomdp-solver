package de.jlandsmannn.DecPOMDPSolver.domain.utility.tuple;

import java.util.Objects;

public class Tuple4<A, B, C, D> extends Tuple3<A, B, C> {
  private final D d;

  protected Tuple4(A a, B b, C c, D d) {
    super(a, b, c);
    this.d = d;
  }

  public D fourth() {
    return d;
  }

  @Override
  public Object get(int i) {
    if (i == 3) return d;
    return super.get(i);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Tuple4<?, ?, ?, ?> tuple4)) return false;
    if (!super.equals(o)) return false;
    return Objects.equals(d, tuple4.d);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), d);
  }
}
