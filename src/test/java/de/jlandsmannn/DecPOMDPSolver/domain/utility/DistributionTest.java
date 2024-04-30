package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import de.jlandsmannn.DecPOMDPSolver.domain.utility.exceptions.DistributionEmptyException;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.exceptions.DistributionSumNotOneException;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class DistributionTest {

  @Test
  void shouldThrowRuntimeExceptionIfDistributionIsEmpty() {
    assertThrows(DistributionEmptyException.class, () ->
      Distribution.of(Map.of())
    );
  }

  @Test
  void shouldThrowRuntimeExceptionIfDistributionSumIsNotOne() {
    assertThrows(DistributionSumNotOneException.class, () ->
      Distribution.of(Map.of(
        'A', 0.5
      ))
    );
  }

  @ParameterizedTest()
  @ValueSource(ints = {1,2,3,4,5,6,7,8,9,10})
  void createUniformDistributionShouldGiveAllItemsSameProbability(int numberOfItems) {
    var inputSet = IntStream.range(0, numberOfItems).boxed().collect(Collectors.toSet());
    var distribution = Distribution.createUniformDistribution(inputSet);
    var expectedProbability = 1D / numberOfItems;

    for (var item : distribution) {
      var actualProbability = distribution.getProbability(item);
      assertEquals(expectedProbability, actualProbability);
    }
  }

  @Test
  void createSingleEntryDistributionShouldCreateDistributionWithOneEntryAndProbability1() {
    var distribution2 = Distribution.createSingleEntryDistribution(1);
    var actualSize = distribution2.size();
    var actualProbability = distribution2.getProbability(1);

    assertEquals(1, actualSize);
    assertEquals(1, actualProbability);
  }

  @Test
  void sizeShouldReturnNumberOfEntries() {
    var distribution = new Distribution<>(Map.of(
      "A", 0.1D,
      "B", 0.2D,
      "C", 0.5D,
      "D", 0.1D,
      "E", 0.1D
    ));
    var actual = distribution.size();
    var expected = 5;
    assertEquals(expected, actual);
  }

  @Test
  void getMaxShouldReturnKeyWithGreatestProbability() {
    var distribution = new Distribution<>(Map.of(
      "A", 0.1D,
      "B", 0.2D,
      "C", 0.5D,
      "D", 0.1D,
      "E", 0.1D
    ));
    var actual = distribution.getMax();
    var expected = "C";
    assertEquals(expected, actual);
  }

  @Test
  void keySet() {
    var distribution = new Distribution<>(Map.of(
      "A", 0.1D,
      "B", 0.2D,
      "C", 0.5D,
      "D", 0.1D,
      "E", 0.1D
    ));
    var actual = distribution.keySet();
    var expected = Set.of("A", "B", "C", "D", "E");
    assertEquals(expected, actual);
  }

  @Test
  void entrySetShouldReturnAllEntries() {
    var distribution = new Distribution<>(Map.of(
      "A", 0.1D,
      "B", 0.2D,
      "C", 0.5D,
      "D", 0.1D,
      "E", 0.1D
    ));
    var actual = distribution.entrySet();
    assertTrue(actual.contains(Map.entry("A", 0.1D)));
    assertTrue(actual.contains(Map.entry("B", 0.2D)));
    assertTrue(actual.contains(Map.entry("C", 0.5D)));
    assertTrue(actual.contains(Map.entry("D", 0.1D)));
    assertTrue(actual.contains(Map.entry("E", 0.1D)));
  }

  @Test
  void getProbabilityShouldReturnProbabilityOfExistingEntry() {
    var distribution = new Distribution<>(Map.of(
      "A", 0.1D,
      "B", 0.2D,
      "C", 0.5D,
      "D", 0.1D,
      "E", 0.1D
    ));
    var actualA = distribution.getProbability("A");
    var actualB = distribution.getProbability("B");
    var actualC = distribution.getProbability("C");
    var actualD = distribution.getProbability("D");
    var actualE = distribution.getProbability("E");
    assertEquals(0.1D, actualA);
    assertEquals(0.2D, actualB);
    assertEquals(0.5D, actualC);
    assertEquals(0.1D, actualD);
    assertEquals(0.1D, actualE);
  }

  @Test
  void getProbabilityShouldReturn0IfElementDoesNotExist() {
    var distribution = new Distribution<>(Map.of(
      "A", 0.1D,
      "B", 0.2D,
      "C", 0.5D,
      "D", 0.1D,
      "E", 0.1D
    ));
    var actual = distribution.getProbability("Z");
    assertEquals(0D, actual);
  }

  @RepeatedTest(5)
  void getRandomShouldReturnWeightedRandomElement() {
    var distribution = new Distribution<>(Map.of(
      "A", 0.1D,
      "B", 0.2D,
      "C", 0.5D,
      "D", 0.1D,
      "E", 0.1D
    ));
    long totalCount = 1_000_000L;
    Map<String, Long> counts = new HashMap<>();

    // repeatedly get random element
    for (long i = 0; i < totalCount; i++) {
      var randomElement = distribution.getRandom();
      var currentCount = counts.getOrDefault(randomElement, 0L);
      counts.put(randomElement, currentCount + 1);
    }

    for (var entry : counts.entrySet()) {
      var expected = distribution.getProbability(entry.getKey());
      var relative = (double) Math.round((float) (100L * entry.getValue()) / totalCount) / 100D;
      assertEquals(expected, relative, entry.getKey() + " was not selected correctly");
    }


  }

  @Test
  void replaceEntryWithDistributionShouldEndUpWithSumOfProbabilitiesOf1() throws DistributionSumNotOneException, DistributionEmptyException {
    var distribution = new Distribution<>(Map.of(
      "A", 0.1D,
      "B", 0.2D,
      "C", 0.5D,
      "D", 0.1D,
      "E", 0.1D
    ));
    var replacement = new Distribution<>(Map.of("D", 0.5D, "E", 0.5D));
    distribution.replaceEntryWithDistribution("C", replacement);
    var sumOfProbabilities = distribution.keySet().stream()
      .map(distribution::getProbability)
      .reduce(Double::sum)
      .orElse(0D);
    assertEquals(1D, sumOfProbabilities);
  }

  @Test
  void replaceEntryWithDistributionShouldDivideProbabilityToNewDistribution() throws DistributionSumNotOneException, DistributionEmptyException {
    var distribution = new Distribution<>(Map.of(
      "A", 0.1D,
      "B", 0.2D,
      "C", 0.5D,
      "D", 0.1D,
      "E", 0.1D
    ));
    var replacement = new Distribution<>(Map.of("B", 0.5D, "E", 0.5D));
    distribution.replaceEntryWithDistribution("C", replacement);
    var probabilityB = distribution.getProbability("B");
    var probabilityE = distribution.getProbability("E");
    assertEquals(0.45D, probabilityB);
    assertEquals(0.35D, probabilityE);
  }
}