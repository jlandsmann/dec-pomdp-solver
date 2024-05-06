package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record State(String name) {

  public static State from(String name) {
    return new State(name);
  }

  public static List<State> listOf(String ...names) {
    return Arrays.stream(names).map(State::from).toList();
  }

  public static Set<State> setOf(String ...names) {
    return Arrays.stream(names).map(State::from).collect(Collectors.toSet());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof State) {
      return name.equals(((State) obj).name);
    }
    return false;
  }

  @Override
  public String toString() {
    return "State " + name;
  }

  @Override
  public int hashCode() {
    return Objects.hash("State", name);
  }
}
