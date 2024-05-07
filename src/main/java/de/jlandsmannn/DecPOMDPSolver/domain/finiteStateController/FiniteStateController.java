package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

import java.util.*;

public class FiniteStateController {
  protected long nodeIndex;
  protected final List<Node> nodes;
  protected final Map<Node, Distribution<Action>> actionFunction;
  protected final Map<Node, Map<Action, Map<Observation, Distribution<Node>>>> transitionFunction;

  public FiniteStateController(List<Node> nodes, Map<Node, Distribution<Action>> actionFunction, Map<Node, Map<Action, Map<Observation, Distribution<Node>>>> transitionFunction) {
    this.nodes = nodes;
    this.nodeIndex = nodes.size();
    this.actionFunction = actionFunction;
    this.transitionFunction = transitionFunction;
  }

  public List<Node> getNodes() {
    return nodes;
  }

  public long getNodeIndex() {
    return nodeIndex;
  }

  public Distribution<Action> getAction(Node q) {
    return actionFunction.get(q);
  }

  public Distribution<Node> getFollowNode(Node q, Action a, Observation o) {
    return transitionFunction.getOrDefault(q, Map.of()).getOrDefault(a, Map.of()).get(o);
  }

  public void addNode(Node node, Action action) {
    addNode(node, Distribution.createSingleEntryDistribution(action));
  }

  public void addNode(Node node, Distribution<Action> action) {
    if (nodes.contains(node)) {
      throw new IllegalArgumentException("Node " + node + " already exists");
    }
    nodes.add(node);
    nodeIndex++;
    actionFunction.put(node, action);
  }

  public void addTransition(Node node, Action a, Observation o, Node followNode) {
    addTransition(node, a, o, Distribution.createSingleEntryDistribution(followNode));
  }

  public void addTransition(Node node, Action a, Observation o, Distribution<Node> transition) {
    if (!nodes.contains(node)) {
      throw new IllegalArgumentException("Node " + node + " does not exist.");
    }
    transitionFunction.putIfAbsent(node, new HashMap<>());
    transitionFunction.get(node).putIfAbsent(a, new HashMap<>());
    transitionFunction.get(node).get(a).put(o, transition);
  }

  public void pruneNodes(Set<Node> nodesToPrune, Distribution<Node> nodesToReplaceWith) {
    // first remove all outgoing connections from nodes to prune
    for (var nodeToPrune : nodesToPrune) {
      nodes.remove(nodeToPrune);
      actionFunction.remove(nodeToPrune);
      transitionFunction.remove(nodeToPrune);
    }

    // update distributions of remaining nodes
    for (var node : transitionFunction.keySet()) {
      for (var action : transitionFunction.get(node).keySet()) {
        for (var observation : transitionFunction.get(node).get(action).keySet()) {
          var distribution = transitionFunction.get(node).get(action).get(observation);
          for (var nodeToPrune : nodesToPrune) {
            distribution.replaceEntryWithDistribution(nodeToPrune, nodesToReplaceWith);
          }
        }
      }
    }
  }

  public void pruneNode(Node nodeToPrune, Distribution<Node> nodesToReplaceWith) {
    nodes.remove(nodeToPrune);
    actionFunction.remove(nodeToPrune);
    transitionFunction.remove(nodeToPrune);
    for (var node : transitionFunction.keySet()) {
      for (var action : transitionFunction.get(node).keySet()) {
        for (var observation : transitionFunction.get(node).get(action).keySet()) {
          var distribution = transitionFunction.get(node).get(action).get(observation);
          distribution.replaceEntryWithDistribution(nodeToPrune, nodesToReplaceWith);
        }
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FiniteStateController that)) return false;
    return Objects.equals(getNodes(), that.getNodes())
      && Objects.equals(actionFunction, that.actionFunction)
      && Objects.equals(transitionFunction, that.transitionFunction);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getNodes(), actionFunction, transitionFunction);
  }
}
