package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record Observation(String name) {

  public static Observation from(String name) {
    return new Observation(name);
  }

  public static Set<Observation> setOf(String ...names) {
    return Arrays.stream(names).map(Observation::from).collect(Collectors.toSet());
  }

  public static List<Observation> listOf(String ...names) {
    return Arrays.stream(names).map(Observation::from).toList();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Observation) {
      return name.equals(((Observation) obj).name);
    }
    return false;
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
