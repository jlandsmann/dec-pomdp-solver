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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof State state)) return false;
    return Objects.equals(name, state.name);
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
