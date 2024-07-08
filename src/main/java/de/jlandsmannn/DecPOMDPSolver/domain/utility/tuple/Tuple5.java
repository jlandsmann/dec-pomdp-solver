package de.jlandsmannn.DecPOMDPSolver.domain.utility.tuple;

import java.util.Objects;

public class Tuple5<A, B, C, D, E> extends Tuple4<A, B, C, D> {
  private final E e;

  protected Tuple5(A a, B b, C c, D d, E e) {
    super(a, b, c, d);
    this.e = e;
  }

  public E fifth() {
    return e;
  }

  @Override
  public Object get(int i) {
    if (i == 4) return e;
    return super.get(i);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Tuple5<?, ?, ?, ?, ?> tuple5)) return false;
    if (!super.equals(o)) return false;
    return Objects.equals(e, tuple5.e);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), e);
  }
}
