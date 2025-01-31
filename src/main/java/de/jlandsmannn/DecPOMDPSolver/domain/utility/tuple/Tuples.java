package de.jlandsmannn.DecPOMDPSolver.domain.utility.tuple;

public interface Tuples {

  public static <A, B> Tuple2<A, B> of(A a, B b) {
    return new Tuple2<>(a, b);
  }

  public static <A, B, C> Tuple3<A, B, C> of(A a, B b, C c) {
    return new Tuple3<>(a, b, c);
  }

  public static <A, B, C, D> Tuple4<A, B, C, D> of(A a, B b, C c, D d) {
    return new Tuple4<>(a, b, c, d);
  }

  public static <A, B, C, D, E> Tuple5<A, B, C, D, E> of(A a, B b, C c, D d, E e) {
    return new Tuple5<>(a, b, c, d, e);
  }



}
