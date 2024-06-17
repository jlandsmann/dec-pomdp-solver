package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.exceptions.DistributionEmptyException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A finite state controller is a graph consisting of nodes.
 * For each node a distribution of actions is given,
 * and for each node and action a distribution of followup nodes is defined,
 * which depends on the observation made after taking the action.
 *
 * This state controller is used by {@link AgentWithStateController}
 * to represent the policy of the agent and to encode
 * the history of the agent space-efficient.
 */
public class FiniteStateController {
  protected AtomicLong nodeIndex;
  protected final List<Node> nodes;
  protected final Map<Node, Distribution<Action>> actionFunction;
  protected final Map<Node, Map<Action, Map<Observation, Distribution<Node>>>> transitionFunction;

  /**
   * Default constructor with nodes, action- and transition function.
   * @param nodes the nodes of this controller
   * @param actionFunction the action function of this controller
   * @param transitionFunction the transition function of this controller
   */
  public FiniteStateController(List<Node> nodes, Map<Node, Distribution<Action>> actionFunction, Map<Node, Map<Action, Map<Observation, Distribution<Node>>>> transitionFunction) {
    this.nodes = Collections.synchronizedList(new ArrayList<>(nodes));
    this.nodeIndex = new AtomicLong(nodes.size());
    this.actionFunction = actionFunction;
    this.transitionFunction = transitionFunction;
  }

  /**
   * Getter for {@link FiniteStateController#nodes}
   * @return the nodes of this controller
   */
  public List<Node> getNodes() {
    return nodes;
  }

  /**
   * This function is used to create uniquely named nodes.
   * The node index increases with each node added to the controller.
   * That is the reason why it can be considered to be safe
   * to create unique nodes containing the index in the name.
   */
  public long getNodeIndex() {
    return nodeIndex.getAndIncrement();
  }

  /**
   * Returns the distribution nodes based on the given node.
   * @param node the node check for
   * @return the distribution of actions
   */
  public Distribution<Action> getActionSelection(Node node) {
    return actionFunction.get(node);
  }

  /**
   * Return
   * @param node
   * @param action
   * @param observation
   * @return
   */
  public Distribution<Node> getTransition(Node node, Action action, Observation observation) {
    return transitionFunction.getOrDefault(node, Map.of()).getOrDefault(action, Map.of()).get(observation);
  }

  public void addNode(Node node, Action action) {
    addNode(node, Distribution.createSingleEntryDistribution(action));
  }

  public void addNode(Node node, Distribution<Action> action) {
    if (nodes.contains(node)) {
      throw new IllegalArgumentException("Node " + node + " already exists");
    }
    nodes.add(node);
    actionFunction.put(node, action);
  }

  public void addTransition(Node node, Action a, Observation o, Node followNode) {
    addTransition(node, a, o, Distribution.createSingleEntryDistribution(followNode));
  }

  public void addTransition(Node node, Action a, Observation o, Distribution<Node> transition) {
    if (!nodes.contains(node)) {
      throw new IllegalArgumentException("Node " + node + " does not exist.");
    }
    transitionFunction.putIfAbsent(node, new ConcurrentHashMap<>());
    transitionFunction.get(node).putIfAbsent(a, new ConcurrentHashMap<>());
    transitionFunction.get(node).get(a).put(o, transition);
  }

  public void pruneNodes(Collection<Node> nodesToPrune, Distribution<Node> nodesToReplaceWith) {
    removeOutgoingConnections(nodesToPrune);
    replaceIncomingConnections(nodesToPrune, nodesToReplaceWith);
  }

  public void pruneNode(Node nodeToPrune, Distribution<Node> nodesToReplaceWith) {
    pruneNodes(Set.of(nodeToPrune), nodesToReplaceWith);
  }

  public void pruneNode(Node nodeToPrune, Node nodeToReplaceWith) {
    pruneNode(nodeToPrune, Distribution.createSingleEntryDistribution(nodeToReplaceWith));
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

  private void removeOutgoingConnections(Collection<Node> nodes) {
    nodes.forEach(this::removeOutgoingConnections);
  }

  private void removeOutgoingConnections(Node node) {
    nodes.remove(node);
    actionFunction.remove(node);
    transitionFunction.remove(node);
  }

  private void replaceIncomingConnections(Collection<Node> nodesToPrune, Distribution<Node> nodesToReplaceWith) {
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

}
