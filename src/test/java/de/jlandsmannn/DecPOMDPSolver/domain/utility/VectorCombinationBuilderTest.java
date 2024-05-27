package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VectorCombinationBuilderTest {
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
  void streamOf_ShouldGenerateStreamOfVectors() {
    var stream = VectorCombinationBuilder.streamOf(listOfLists);
    var actualCombinationCount = stream.count();
    assertEquals(numberOfCombinations, actualCombinationCount);
  }

  @Test
  void streamOf_ShouldGenerateOrderedStreamOfVectors() {
    var stream = VectorCombinationBuilder.streamOf(listOfLists);
    var firstVector = stream.findFirst().orElseThrow();
    assertEquals(firstVector.get(0), "A1");
    assertEquals(firstVector.get(1), "B1");
    assertEquals(firstVector.get(2), "C1");

    stream = VectorCombinationBuilder.streamOf(listOfLists);
    var secondVector = stream.skip(1).findFirst().orElseThrow();
    assertEquals(secondVector.get(0), "A1");
    assertEquals(secondVector.get(1), "B1");
    assertEquals(secondVector.get(2), "C2");


    stream = VectorCombinationBuilder.streamOf(listOfLists);
    var anotherVector = stream.skip(4).findFirst().orElseThrow();
    assertEquals(anotherVector.get(0), "A1");
    assertEquals(anotherVector.get(1), "B2");
    assertEquals(anotherVector.get(2), "C1");
  }
}