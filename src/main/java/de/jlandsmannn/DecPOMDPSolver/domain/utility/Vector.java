package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.*;
import java.util.stream.Stream;

/**
 * A vector represents a rich-feature list of elements.
 * In the context of DecPOMDP vectors often describe
 * a list of elements where each element is assigned to a specific element of a set.
 * For example, this is used by {@link de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP}
 * to declare which agent selected which action.
 *
 * @param <T> the data type of the elements
 */
public class Vector<T> {
  private final List<T> values;

  /**
   * This method creates a vector containing all elements given.
   *
   * @param elements the elements of the vector
   * @return a vector containing all elements
   * @param <U> the data type of the elements
   */
  public static <U> Vector<U> of(U... elements) {
    return of(List.of(elements));
  }

  /**
   * This method creates a vector containing all elements given.
   *
   * @param elements the elements of the vector
   * @return a vector containing all elements
   * @param <U> the data type of the elements
   */
  public static <U> Vector<U> of(SequencedCollection<U> elements) {
    return new Vector<>(elements);
  }

  /**
   * This method creates a new vector based on the given on.
   * But at the given index, the given element is inserted.
   * All elements, starting from the given index,
   * will be shifted to the right by 1.
   *
   * @param vector the vector to copy from
   * @param index the index to insert the new element
   * @param elementToAdd the element to add to the new vector
   * @return a new vector containing the new element
   * @param <U> the data type of the elements
   */
  public static <U> Vector<U> addEntry(Vector<U> vector, int index, U elementToAdd) {
    var list = new ArrayList<>(vector.values);
    list.add(index, elementToAdd);
    return new Vector<>(list);
  }

  /**
   * This constructor creates a vector containing all elements given.
   *
   * @param elements the elements of the vector
   */
  public Vector(SequencedCollection<T> elements) {
    this.values = List.copyOf(elements);
    if (this.values.isEmpty()) {
      throw new IllegalArgumentException("Vector is empty");
    }
  }

  /**
   * Gets the element at the given index
   * @param index the index to get the element from
   * @return the element at the given index
   */
  public T get(int index) {
    return values.get(index);
  }

  /**
   * @return the number of elements of this vector
   */
  public int size() {
    return values.size();
  }

  /**
   * @return whether the vector is empty or not
   */
  public boolean isEmpty() {
    return values.isEmpty();
  }

  /**
   * @param element the element to look for
   * @return whether the given element is contained in this vector or not
   */
  public boolean contains(T element) {
    return values.contains(element);
  }

  /**
   * @return a stream consisting of all elements of this vector
   */
  public Stream<T> stream() {
    return values.stream();
  }

  /**
   * @return a set consisting of all elements of this vector
   */
  public Set<T> toSet() {
    return Set.copyOf(values);
  }

  /**
   * @return a list consisting of all elements of this vector
   */
  public List<T> toList() {
    return List.copyOf(values);
  }

  /**
   * @return a array consisting of all elements of this vector
   */
  public Object[] toArray() {
    return values.toArray();
  }

  /**
   * @return a array consisting of all elements of this vector
   */
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
