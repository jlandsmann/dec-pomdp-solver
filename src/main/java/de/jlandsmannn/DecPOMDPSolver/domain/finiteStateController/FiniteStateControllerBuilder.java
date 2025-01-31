package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class can be used to build a {@link FiniteStateController} step by step.
 * It does not business logic validation.
 */
public class FiniteStateControllerBuilder {
  private final List<Node> nodes = new ArrayList<>();
  private final Map<Node, Distribution<Action>> actionFunction = new ConcurrentHashMap<>();
  private final Map<Node, Map<Action, Map<Observation, Distribution<Node>>>> transitionFunction = new ConcurrentHashMap<>();

  /**
   * This function creates an arbitrary controller with a single node,
   * with a uniform action distribution, where each observation
   * leads back to the original node.
   * It is a special case of {@link FiniteStateControllerBuilder#createArbitraryController(String, List, int, List, List)} with nodeCount = 1.
   *
   * @param name             the name of the controller can be used to differentiate nodes of two controllers
   * @param actions          the actions to consider
   * @param observations     the observations to consider
   * @return the created controller
   */
  public static FiniteStateController createArbitraryController(String name, List<Action> actions, List<Observation> observations) {
    return createArbitraryController(name, actions, observations, 1);
  }

  /**
   * This function creates an arbitrary controller with a nodeCounts node,
   * with a uniform action distribution, where each observation leads to the next node.
   * Observations taken, after taking action in the last node, lead back to the first node.
   *
   * @param name             the name of the controller can be used to differentiate nodes of two controllers
   * @param actions          the actions to consider
   * @param observations     the observations to consider
   * @param nodeCount        the number of nodes to create
   * @return the created controller
   */
  public static FiniteStateController createArbitraryController(String name, List<Action> actions, List<Observation> observations, int nodeCount) {
    var builder = new FiniteStateControllerBuilder();
    var actionDistribution = Distribution.createUniformDistribution(actions);
    for (int i = 0; i < nodeCount; i++) {
      Node node = Node.from(name + "-Q" + i);
      Node followNode = Node.from(name + "-Q" + ((i + 1) % nodeCount));
      builder.addNode(node).addActionSelection(node, actionDistribution);
      for (var action : actionDistribution.keySet()) {
        for (var observation : observations) {
          builder.addTransition(node, action, observation, Distribution.createSingleEntryDistribution(followNode));
        }
      }
    }
    return builder.createFiniteStateController();
  }

  /**
   * Creates a controller with a single node and the given action selection,
   * which loops on itself for all actions and observations.
   * @param name             the name of the controller can be used to differentiate nodes of two controllers
   * @param actions          the actions to consider
   * @param observations     the observations to consider
   * @param actionSelection  the action selection for the sole node
   * @return the created controller
   */
  public static FiniteStateController createSelfLoopController(String name, List<Action> actions, List<Observation> observations, Distribution<Action> actionSelection) {
    var builder = new FiniteStateControllerBuilder();
    var node = Node.from(name + "-Q0");
    var followNode = Distribution.createSingleEntryDistribution(node);

    builder.addNode(node).addActionSelection(node, actionSelection);
    for (var action : actions) {
      for (var observation : observations) {
        builder.addTransition(node, action, observation, followNode);
      }
    }
    return builder.createFiniteStateController();
  }

  public FiniteStateControllerBuilder addNode(Node node) {
    this.nodes.remove(node);
    this.nodes.add(node);
    return this;
  }

  public FiniteStateControllerBuilder addNodes(Collection<Node> nodes) {
    this.nodes.removeAll(nodes);
    this.nodes.addAll(nodes);
    return this;
  }

  public FiniteStateControllerBuilder addActionSelection(Node node, Distribution<Action> actions) {
    this.actionFunction.put(node, actions);
    return this;
  }

  public FiniteStateControllerBuilder addTransition(Node node, Action action, Observation observation, Distribution<Node> nextNodeDistribution) {
    this.transitionFunction.putIfAbsent(node, new ConcurrentHashMap<>());
    this.transitionFunction.get(node).putIfAbsent(action, new ConcurrentHashMap<>());
    this.transitionFunction.get(node).get(action).put(observation, nextNodeDistribution);
    return this;
  }

  public FiniteStateController createFiniteStateController() {
    return new FiniteStateController(nodes, actionFunction, transitionFunction);
  }
}