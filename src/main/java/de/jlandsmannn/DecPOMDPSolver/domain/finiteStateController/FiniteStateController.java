package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

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
  protected final List<Node> nodes;
  protected final Map<Node, Distribution<Action>> actionFunction;
  protected final Map<Node, Map<Action, Map<Observation, Distribution<Node>>>> transitionFunction;
  protected final Map<Node, Map<Node, Integer>> followNodes;
  protected AtomicLong nodeIndex;

  /**
   * Default constructor with nodes, action- and transition function.
   *
   * @param nodes              the nodes of this controller
   * @param actionFunction     the action function of this controller
   * @param transitionFunction the transition function of this controller
   */
  public FiniteStateController(List<Node> nodes, Map<Node, Distribution<Action>> actionFunction, Map<Node, Map<Action, Map<Observation, Distribution<Node>>>> transitionFunction) {
    this.nodes = Collections.synchronizedList(new ArrayList<>(nodes));
    this.nodeIndex = new AtomicLong(nodes.size());
    this.actionFunction = actionFunction;
    this.transitionFunction = transitionFunction;
    this.followNodes = new ConcurrentHashMap<>();

    initFollowNodes(transitionFunction);
  }

  /**
   * Getter for {@link FiniteStateController#nodes}
   *
   * @return the nodes of this controller
   */
  public List<Node> getNodes() {
    return nodes;
  }

  public List<Node> getFollowNodes(Node node) {
    if (!nodes.contains(node)) {
      throw new IllegalArgumentException("Node " + node + " does not exist in controller");
    } else if (!followNodes.containsKey(node)) {
      throw new IllegalStateException("Node " + node + " does not have transitions defined");
    }
    return List.copyOf(Set.copyOf(followNodes.get(node).keySet()));
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
   * Returns the probability selecting the given action in the given node.
   *
   * @param node the node to check for
   * @param action the action to check for
   * @return the probability of selecting the action in the given node
   */
  public double getActionSelectionProbability(Node node, Action action) {
    return Optional
      .ofNullable(actionFunction.get(node))
      .map(t -> t.getProbability(action))
      .orElse(0D);
  }

  /**
   * Returns the probability of transitioning into a given node,
   * based on the given current node, selected action and sensed observation.
   *
   * @param node the node to start from
   * @param action the action selected
   * @param observation the observation made
   * @param followNode the node to transition to
   * @return the probability of the transition
   */
  public double getTransitionProbability(Node node, Action action, Observation observation, Node followNode) {
    return Optional
      .ofNullable(transitionFunction.get(node))
      .map(t -> t.get(action))
      .map(t -> t.get(observation))
      .map(t -> t.getProbability(followNode))
      .orElse(0D);
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

    followNodes.putIfAbsent(node, new ConcurrentHashMap<>());
    for (var followNode : transition.keySet()) {
      followNodes.get(node).merge(followNode, 1, Integer::sum);
    }
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
    followNodes.remove(node);
  }

  private void replaceIncomingConnections(Collection<Node> nodesToPrune, Distribution<Node> nodesToReplaceWith) {
    for (var node : transitionFunction.keySet()) {
      for (var action : transitionFunction.get(node).keySet()) {
        for (var distribution : transitionFunction.get(node).get(action).values()) {
          for (var nodeToPrune : nodesToPrune) {
            if (distribution.getProbability(nodeToPrune) <= 0) continue;
            var newFollower = new ArrayList<>(nodesToReplaceWith.keySet());
            newFollower.removeAll(distribution.keySet());
            distribution.replaceEntryWithDistribution(nodeToPrune, nodesToReplaceWith);
            if (followNodes.get(node).getOrDefault(nodeToPrune, 0) <= 1) {
              followNodes.get(node).remove(nodeToPrune);
            } else {
              followNodes.get(node).merge(nodeToPrune, -1, Integer::sum);
            }
            for (var followNode : newFollower) {
              followNodes.get(node).merge(followNode, 1, Integer::sum);
            }
          }
        }
      }
    }
  }

  private void initFollowNodes(Map<Node, Map<Action, Map<Observation, Distribution<Node>>>> transitionFunction) {
    for (Node node : transitionFunction.keySet()) {
      followNodes.putIfAbsent(node, new ConcurrentHashMap<>());
      for (Action action : transitionFunction.get(node).keySet()) {
        for (Observation observation : transitionFunction.get(node).get(action).keySet()) {
          var follower = transitionFunction.get(node).get(action).get(observation).keySet();
          for (var followNode : follower) {
            followNodes.get(node).merge(followNode, 1, Integer::sum);
          }
        }
      }
    }
  }
}
