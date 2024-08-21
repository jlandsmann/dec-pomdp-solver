package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import de.jlandsmannn.DecPOMDPSolver.domain.utility.exceptions.DistributionSumNotOneException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;

import static org.junit.jupiter.api.Assertions.*;

class DistributionCollectorTest {

  DistributionCollector<String> collector;

  @BeforeEach
  void setUp() {
    collector = new DistributionCollector<>();
  }

  @Test
  void supplier_ShouldReturnEmptyMap() {
    var expected = Map.of();
    var actual = collector.supplier().get();

    assertEquals(expected, actual);
  }

  @Test
  void accumulator_ShouldAddEntryToMap() {
    var expected = Map.of("A", 0.25);
    var actual = new HashMap<String, Double>();
    collector.accumulator().accept(actual, Map.entry("A", 0.25));

    assertEquals(expected, actual);
  }

  @Test
  void accumulator_ShouldAddProbabilityIfEntryAlreadyExistsInMap() {
    var expected = Map.of("A", 0.65);
    var actual = new HashMap<String, Double>();
    actual.put("A", 0.5);
    collector.accumulator().accept(actual, Map.entry("A", 0.15));

    assertEquals(expected, actual);
  }

  @Test
  void combiner_ShouldMergeKeysOfTwoMaps() {
    var originalA = new HashMap<>(Map.of("A", 0.25, "B", 0.1, "C", 0.2));
    var originalB = new HashMap<>(Map.of("F", 0.25, "G", 0.1, "H", 0.2));

    var combined = collector.combiner().apply(originalA, originalB);

    assertTrue(combined.containsKey("A"));
    assertTrue(combined.containsKey("B"));
    assertTrue(combined.containsKey("C"));
    assertTrue(combined.containsKey("F"));
    assertTrue(combined.containsKey("G"));
    assertTrue(combined.containsKey("H"));
  }

  @Test
  void combiner_ShouldMergeValuesOfTwoMaps() {
    var originalA = new HashMap<>(Map.of("A", 0.25, "B", 0.1, "C", 0.2));
    var originalB = new HashMap<>(Map.of("F", 0.25, "G", 0.1, "H", 0.2));

    var combined = collector.combiner().apply(originalA, originalB);

    assertEquals(0.25, combined.get("A"));
    assertEquals(0.1, combined.get("B"));
    assertEquals(0.2, combined.get("C"));
    assertEquals(0.25, combined.get("F"));
    assertEquals(0.1, combined.get("G"));
    assertEquals(0.2, combined.get("H"));
  }

  @Test
  void combiner_ShouldAddValuesOfElementsInTwoMaps() {
    var originalA = new HashMap<>(Map.of("A", 0.25, "B", 0.1, "C", 0.2));
    var originalB = new HashMap<>(Map.of("A", 0.25, "C", 0.1, "H", 0.2));

    var combined = collector.combiner().apply(originalA, originalB);

    assertEquals(0.5, combined.get("A"), 1e-6);
    assertEquals(0.1, combined.get("B"), 1e-6);
    assertEquals(0.3, combined.get("C"), 1e-6);
    assertEquals(0.2, combined.get("H"), 1e-6);
  }

  @Test
  void finisher_ShouldReturnDistributionRepresentingMap() {
    var rawDistribution = Map.of(
      "A", 0.25,
      "B", 0.1,
      "C", 0.2,
      "D", 0.05,
      "E", 0.3,
      "F", 0.1
    );
    var expected = Distribution.of(rawDistribution);
    var actual = collector.finisher().apply(rawDistribution);
    assertEquals(expected, actual);
  }

  @Test
  void finisher_ShouldThrowIfNotNormalizeAndPartialDistributionGiven() {
    collector = new DistributionCollector<>(false);
    var rawDistribution = Map.of(
      "A", 0.25,
      "B", 0.1,
      "C", 0.2,
      "D", 0.05,
      "E", 0.3
    );
    assertThrows(DistributionSumNotOneException.class, () -> collector.finisher().apply(rawDistribution));
  }

  @Test
  void finisher_ShouldNormalizeIfNormalizeAndPartialDistributionGiven() {
    collector = new DistributionCollector<>(true);
    var rawDistribution = new HashMap<>(Map.of(
      "A", 0.3,
      "B", 0.1
    ));
    var actual = collector.finisher().apply(rawDistribution);

    assertEquals(0.75, actual.getProbability("A"), 1e-6);
    assertEquals(0.25, actual.getProbability("B"), 1e-6);
  }

  @Test
  void characteristics_ShouldIncludeConcurrent() {
    var characteristics = collector.characteristics();
    assertTrue(characteristics.contains(Collector.Characteristics.CONCURRENT));
  }

  @Test
  void characteristics_ShouldIncludeUnordered() {
    var characteristics = collector.characteristics();
    assertTrue(characteristics.contains(Collector.Characteristics.UNORDERED));
  }

  @Test
  void characteristics_ShouldNotIncludeIdentityFinish() {
    var characteristics = collector.characteristics();
    assertFalse(characteristics.contains(Collector.Characteristics.IDENTITY_FINISH));
  }
}