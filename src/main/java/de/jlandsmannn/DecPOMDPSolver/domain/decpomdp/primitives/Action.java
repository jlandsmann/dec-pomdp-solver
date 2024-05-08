package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record Action(String name) {

  public static Action from(String name) {
    return new Action(name);
  }

  public static List<Action> listOf(String ...names) {
    return Arrays.stream(names).map(Action::from).toList();
  }

  public static Set<Action> setOf(String ...names) {
    return Arrays.stream(names).map(Action::from).collect(Collectors.toSet());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Action action)) return false;
    return Objects.equals(name, action.name);
  }

  @Override
  public String toString() {
    return "Action " + name;
  }

  @Override
  public int hashCode() {
    return Objects.hash("Action", name);
  }
}
