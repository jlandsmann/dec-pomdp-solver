package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class HistogramCollectorTest {
  Map<String, Integer> stringAmounts;

  HistogramCollector<String> collector;

  @BeforeEach
  void setUp() {
    collector = new HistogramCollector<>();
    stringAmounts = Map.of(
      "A", 2,
      "B", 3,
      "C", 1,
      "D", 1
    );
  }

  @Test
  void collect_ShouldCountElementsFromStream() {
    var stream = Stream.of("A", "B", "A", "C", "B", "B", "D");
    var expectedHistogram = Histogram.of(stringAmounts);
    var acutalHistogram = stream.collect(new HistogramCollector<>());

    assertEquals(expectedHistogram, acutalHistogram);
  }

  @Test
  void supplier_ShouldReturnEmptyHistogram() {
    var expected = new Histogram<String>();
    var actual = collector.supplier().get();

    assertEquals(expected, actual);
  }

  @Test
  void accumulator_ShouldIncreaseElementCountInHistogram() {
    var element = "A";
    var histogram = new Histogram<String>();
    var originalCount = histogram.get(element);
    collector.accumulator().accept(histogram, element);
    var newCount = histogram.get(element);

    assertEquals(originalCount + 1, newCount);
  }

  @Test
  void combiner_ShouldMergeTwoHistograms() {
    var histogramA = Histogram.of(stringAmounts);
    var histogramB = Histogram.of(stringAmounts);

    var histogramC = collector.combiner().apply(histogramA, histogramB);

    for (var entry : stringAmounts.entrySet()) {
      var element = entry.getKey();
      var originalAmount = entry.getValue();
      int expected = 2 * originalAmount;
      int actual = histogramC.get(element);
      assertEquals(expected, actual);
    }
  }

  @Test
  void finisher_ShouldReturnIdentity() {
    var histogram = Histogram.of(stringAmounts);
    var actual = collector.finisher().apply(histogram);

    assertSame(histogram, actual);
  }

  @Test
  void characteristics_ShouldIncludeUnordered() {
    var characteristics = collector.characteristics();
    assertTrue(characteristics.contains(Collector.Characteristics.UNORDERED));
  }

  @Test
  void characteristics_ShouldIncludeIdentityFinish() {
    var characteristics = collector.characteristics();
    assertTrue(characteristics.contains(Collector.Characteristics.IDENTITY_FINISH));
  }

  @Test
  void characteristics_ShouldNotIncludeConcurrent() {
    var characteristics = collector.characteristics();
    assertFalse(characteristics.contains(Collector.Characteristics.CONCURRENT));
  }
}