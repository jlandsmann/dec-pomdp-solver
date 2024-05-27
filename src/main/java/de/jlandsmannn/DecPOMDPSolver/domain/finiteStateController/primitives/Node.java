package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record Node(String name) {
  public static Node from(String name) {
    return new Node(name);
  }

  public static List<Node> listOf(String ...names) {
    return Arrays.stream(names).map(Node::from).toList();
  }

  public static Set<Node> setOf(String ...names) {
    return Arrays.stream(names).map(Node::from).collect(Collectors.toSet());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Node node)) return false;
    return Objects.equals(name, node.name);
  }

  @Override
  public String toString() {
    return "Node " + name;
  }

  @Override
  public int hashCode() {
    return Objects.hash("Node", name);
  }
}

