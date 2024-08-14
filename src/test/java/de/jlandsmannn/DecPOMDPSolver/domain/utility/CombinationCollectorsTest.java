package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CombinationCollectorsTest {

  Stream<List<String>> streamOfCombinations;
  @BeforeEach
  void setUp() {
    streamOfCombinations = Stream.of(
      List.of("A1", "A2"),
      List.of("B1", "B2")
    );

  }

  @Test
  void toCombinationVectors_ShouldProduceVectorsWithSameAmountOfElementsAsElementsInStream() {
    var combinationVectors = streamOfCombinations.collect(CombinationCollectors.toCombinationVectors()).toList();
    var expectedSize = 2;
    for (var combinationVector : combinationVectors) {
      assertEquals(expectedSize, combinationVector.size());
    }
  }

  @Test
  void toCombinationVectors_ShouldProduceAllPossibleVectors() {
    var combinationVectors = streamOfCombinations.collect(CombinationCollectors.toCombinationVectors()).toList();
    var expectedVectors = List.of(
      Vector.of("A1", "B1"),
      Vector.of("A1", "B2"),
      Vector.of("A2", "B1"),
      Vector.of("A2", "B2")
    );
    assertTrue(combinationVectors.containsAll(expectedVectors));
    assertTrue(expectedVectors.containsAll(combinationVectors));
  }

  @Test
  void toCombinationList_ShouldProduceVectorsWithSameAmountOfElementsAsElementsInStream() {
    var combinationVectors = streamOfCombinations.collect(CombinationCollectors.toCombinationLists()).toList();
    var expectedSize = 2;
    for (var combinationVector : combinationVectors) {
      assertEquals(expectedSize, combinationVector.size());
    }
  }

  @Test
  void toCombinationLists_ShouldProduceAllPossibleVectors() {
    var combinationVectors = streamOfCombinations.collect(CombinationCollectors.toCombinationLists()).toList();
    var expectedVectors = List.of(
      List.of("A1", "B1"),
      List.of("A1", "B2"),
      List.of("A2", "B1"),
      List.of("A2", "B2")
    );
    assertTrue(combinationVectors.containsAll(expectedVectors));
    assertTrue(expectedVectors.containsAll(combinationVectors));
  }
}