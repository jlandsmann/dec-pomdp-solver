package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

import java.util.Map;
import java.util.Set;

public class CorrelationDevice {
  protected final Set<Node> nodes;
  protected final Map<Node, Distribution<Node>> transitionFunction;

  public CorrelationDevice(Set<Node> nodes, Map<Node, Distribution<Node>> transitionFunction) {
    this.nodes = nodes;
    this.transitionFunction = transitionFunction;
  }

  public static CorrelationDevice createArbitrary() {
    var node = new Node("C1");
    var transition = Distribution.createSingleEntryDistribution(node);
    return new CorrelationDevice(Set.of(node), Map.of(node, transition));

  }

  public Distribution<Node> getFollowNode(Node q) {
    return transitionFunction.get(q);
  }
}
