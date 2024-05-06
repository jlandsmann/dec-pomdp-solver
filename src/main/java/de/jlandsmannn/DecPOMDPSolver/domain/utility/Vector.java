package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.*;
import java.util.stream.Stream;

public class Vector<T> implements Iterable<T> {
  private final List<T> values;

  public static <U> Vector<U> of(U ...items) {
    return of(List.of(items));
  }

  public static <U> Vector<U> of(SequencedCollection<U> items) {
    return new Vector<>(items);
  }

  public static <U> Vector<U> addEntry(Vector<U> vector, int index, U value) {
    var list = new ArrayList<>(vector.values);
    list.add(index, value);
    return new Vector<>(list);
  }

  public Vector(T[] values) {
    this(Arrays.asList(values));
  }

  public Vector(SequencedCollection<T> values) {
    this.values = List.copyOf(values);
    if (this.values.isEmpty()) {
      throw new IllegalArgumentException("Vector is empty");
    }
  }

  public T get(int index) {
    return values.get(index);
  }

  public int size() {
    return values.size();
  }

  public boolean isEmpty() {
    return values.isEmpty();
  }

  public boolean contains(T o) {
    return values.contains(o);
  }

  @Override
  public Iterator<T> iterator() {
    return values.iterator();
  }

  public Stream<T> stream() {
    return values.stream();
  }

  public Object[] toArray() {
    return values.toArray();
  }

  public T[] toArray(T[] a) {
    return values.toArray(a);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Vector<?>) {
      return values.equals(((Vector<?>) obj).values);
    }
    return super.equals(obj);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < values.size(); i++) {
      T value = values.get(i);
      sb.append(value);
      if (i + 1 < values.size()) sb.append(", ");
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash("Vector", values);
  }
}
