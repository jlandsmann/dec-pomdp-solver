package de.jlandsmannn.DecPOMDPSolver;

import org.junit.jupiter.api.Assertions;

public class ApplicationAssertions {
  public static void assertFuzzyEquals(double a, double b) {
    assertFuzzyEquals(a,b, null);
  }
  public static void assertFuzzyEquals(double a, double b, String message) {
    assertFuzzyEquals(a,b, 1e-8, message);
  }
  public static void assertFuzzyEquals(double a, double b, double epsilon) {
    assertFuzzyEquals(a,b, epsilon, null);
  }

  public static void assertFuzzyEquals(double a, double b, double epsilon, String message) {
      var diff = Math.abs(a - b);
      Assertions.assertTrue(diff < epsilon, message);
  }


}
