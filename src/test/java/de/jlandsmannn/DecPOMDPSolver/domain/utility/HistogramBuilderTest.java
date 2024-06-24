package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistogramBuilderTest {

  private HistogramBuilder<String> builder;
  private List<String> buckets = List.of("A", "B", "C", "D");

  @BeforeEach
  void setUp() {
    builder = new HistogramBuilder<>();
  }

  @Test
  void of_ShouldCreateListWithAllHistograms() {
    var histograms = builder.of(buckets, 16);
    var expectedNumberOfHistograms = 969; // (numberOfElements + bucketCount - 1) over (bucketCount - 1)
    assertEquals(expectedNumberOfHistograms, histograms.size());
  }

  @Test
  void of_ShouldCreateListWithSteppedHistograms() {
    var histograms = builder.of(buckets, 16, 2);
    var expectedNumberOfHistograms = 165; // ((numberOfElements / stepSize) + bucketCount - 1) over (bucketCount - 1)
    assertEquals(expectedNumberOfHistograms, histograms.size());
  }

}