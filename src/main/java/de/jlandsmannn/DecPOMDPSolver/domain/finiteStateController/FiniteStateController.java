package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FiniteStateController {
  protected final Set<Node> nodes;
  protected final Map<Node, Distribution<Action>> actionFunction;
  protected final Map<Node, Map<Action, Map<Observation, Distribution<Node>>>> transitionFunction;

  public FiniteStateController(Set<Node> nodes, Map<Node, Distribution<Action>> actionFunction, Map<Node, Map<Action, Map<Observation, Distribution<Node>>>> transitionFunction) {
    this.nodes = nodes;
    this.actionFunction = actionFunction;
    this.transitionFunction = transitionFunction;
  }

  public List<Node> getNodes() {
    return List.copyOf(nodes);
  }

  public Distribution<Action> getAction(Node q) {
    return actionFunction.get(q);
  }

  public Distribution<Node> getFollowNode(Node q, Action a, Observation o) {
    return transitionFunction.get(q).get(a).get(o);
  }

  public void pruneNodes(Map<Node, Distribution<Node>> nodesToPrune) {
    // first remove all outgoing connections from nodes to prune
    for (var nodeToPrune : nodesToPrune.keySet()) {
      nodes.remove(nodeToPrune);
      actionFunction.remove(nodeToPrune);
      transitionFunction.remove(nodeToPrune);
    }

    // update distributions of remaining nodes
    for (var node : transitionFunction.keySet()) {
      for (var action : transitionFunction.get(node).keySet()) {
        for (var observation : transitionFunction.get(node).get(action).keySet()) {
          var distribution = transitionFunction.get(node).get(action).get(observation);
          for (var nodeToPrune : nodesToPrune.keySet()) {
            distribution.replaceEntryWithDistribution(nodeToPrune, nodesToPrune.get(nodeToPrune));
          }
        }
      }
    }
  }
}
