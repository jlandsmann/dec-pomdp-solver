package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VectorTest {
  List<Integer> originalCollection;
  Vector<Integer> vector;

  @BeforeEach
  void beforeEach() {
    originalCollection = List.of(1,2,3,4);
    vector = new Vector<>(originalCollection);
  }

  @Test
  void get_ShouldReturnCorrectElementFromOriginalCollection() {
    var actual = vector.get(1);
    var expected = 2;
    assertEquals(expected, actual);
  }

  @Test
  void size_ShouldReturnTheSizeOfTheOriginalCollection() {
    var actual = vector.size();
    var expected = 4;
    assertEquals(expected, actual);
  }

  @Test
  void equals_ShouldReturnTrueForSameObject() {
    assertEquals(vector, vector);
  }

  @Test
  void equals_ShouldReturnTrueForVectorWithSameElementsInSameOrder() {
    var vector1 = new Vector<>(originalCollection);
    var vector2 = new Vector<>(originalCollection);
    assertEquals(vector1, vector2);
  }

  @Test
  void equals_ShouldReturnFalseForVectorWithSameElementsInDifferentOrder() {
    var vector1 = new Vector<>(originalCollection);
    var reverseCollection = new ArrayList<>(originalCollection);
    Collections.reverse(reverseCollection);
    var vector2 = new Vector<>(reverseCollection);
    assertNotEquals(vector1, vector2);
  }

  @Test
  void toString_ShouldConcatenateItsValues() {
    var actual = vector.toString();
    for (int i = 0; i < vector.size(); i++) {
      assertTrue(actual.contains(vector.get(i).toString()));
    }
  }

  @Test
  void hashCode_ShouldBeEqualForVectorWithSameElementsInSameOrder() {
    var vector1 = new Vector<>(originalCollection);
    var vector2 = new Vector<>(originalCollection);
    assertEquals(vector1.hashCode(), vector2.hashCode());

  }

  @Test
  void hashCode_ShouldNotBeEqualForVectorWithSameElementsInDifferentOrder() {
    var vector1 = new Vector<>(originalCollection);
    var reverseCollection = new ArrayList<>(originalCollection);
    Collections.reverse(reverseCollection);
    var vector2 = new Vector<>(reverseCollection);
    assertNotEquals(vector1.hashCode(), vector2.hashCode());
  }
}