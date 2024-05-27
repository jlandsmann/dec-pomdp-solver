package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class VectorStreamBuilderTest {
  private List<List<String>> listOfLists;
  private long numberOfCombinations;

  @BeforeEach
  void setUp() {
    listOfLists = List.of(
      List.of("A1", "A2", "A3", "A4"),
      List.of("B1", "B2", "B3", "B4"),
      List.of("C1", "C2", "C3", "C4")
    );
    numberOfCombinations = 64;
  }

  @Test
  void forEachCombination_ShouldGenerateStreamOfVectors() {
    var stream = VectorStreamBuilder.forEachCombination(listOfLists);
    var actualCombinationCount = stream.count();
    assertEquals(numberOfCombinations, actualCombinationCount);
  }

  @Test
  void forEachCombination_ShouldGenerateOrderedStreamOfVectors() {
    var stream = VectorStreamBuilder.forEachCombination(listOfLists);
    var firstVector = stream.findFirst().orElseThrow();
    assertTrue(firstVector.contains("A1"));
    assertTrue(firstVector.contains("B1"));
    assertTrue(firstVector.contains("C1"));

    stream = VectorStreamBuilder.forEachCombination(listOfLists);
    var secondVector = stream.skip(1).findFirst().orElseThrow();
    assertTrue(secondVector.contains("A1"));
    assertTrue(secondVector.contains("B1"));
    assertTrue(secondVector.contains("C2"));


    stream = VectorStreamBuilder.forEachCombination(listOfLists);
    var anotherVector = stream.skip(4).findFirst().orElseThrow();
    assertTrue(anotherVector.contains("A1"));
    assertTrue(anotherVector.contains("B2"));
    assertTrue(anotherVector.contains("C1"));
  }
}