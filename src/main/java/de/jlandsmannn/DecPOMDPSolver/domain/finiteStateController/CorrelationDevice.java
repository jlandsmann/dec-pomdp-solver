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

  public void addNode(Node node, Node followNode) {
    addNode(node, Distribution.createSingleEntryDistribution(followNode));
  }

  public void addNode(Node node, Distribution<Node> followNodes) {
    nodes.add(node);
  }

  public void pruneNodes(Map<Node, Distribution<Node>> nodesToPrune) {
    // first remove all outgoing connections from nodes to prune
    for (var nodeToPrune : nodesToPrune.keySet()) {
      nodes.remove(nodeToPrune);
      transitionFunction.remove(nodeToPrune);
    }

    // update distributions of remaining nodes
    for (var node : transitionFunction.keySet()) {
      var distribution = transitionFunction.get(node);
      for (var nodeToPrune : nodesToPrune.keySet()) {
        distribution.replaceEntryWithDistribution(nodeToPrune, nodesToPrune.get(nodeToPrune));
      }
    }
  }

  public void pruneNode(Node nodeToPrune, Distribution<Node> nodesToReplaceWith) {
    nodes.remove(nodeToPrune);
    transitionFunction.remove(nodeToPrune);
    for (var node : transitionFunction.keySet()) {
      var distribution = transitionFunction.get(node);
      distribution.replaceEntryWithDistribution(nodeToPrune, nodesToReplaceWith);
    }
  }
}
