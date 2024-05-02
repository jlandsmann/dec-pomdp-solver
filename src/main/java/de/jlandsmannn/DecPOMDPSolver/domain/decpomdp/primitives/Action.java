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

  public static Set<Action> setOf(String ...names) {
    return Arrays.stream(names).map(Action::from).collect(Collectors.toSet());
  }

  public static List<Action> listOf(String ...names) {
    return Arrays.stream(names).map(Action::from).toList();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Action) {
      return name.equals(((Action) obj).name);
    }
    return false;
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
