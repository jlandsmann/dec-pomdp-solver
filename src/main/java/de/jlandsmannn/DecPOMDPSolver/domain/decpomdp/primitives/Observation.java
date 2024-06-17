package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is a simple string-wrapping class
 * to represent an observation of an {@link de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent}
 */
public record Observation(String name) {

  public static Observation from(String name) {
    return new Observation(name);
  }

  public static List<Observation> listOf(String... names) {
    return Arrays.stream(names).map(Observation::from).toList();
  }

  public static Set<Observation> setOf(String... names) {
    return Arrays.stream(names).map(Observation::from).collect(Collectors.toSet());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Observation that)) return false;
    return Objects.equals(name, that.name);
  }

  @Override
  public String toString() {
    return "Observation " + name;
  }

  @Override
  public int hashCode() {
    return Objects.hash("Observation", name);
  }
}
