package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.exceptions.DistributionSumNotOneException;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.exceptions.DistributionEmptyException;

import java.util.*;

public class FiniteStateControllerBuilder {
  private final Set<Node> nodes = new HashSet<>();
  private final Map<Node, Distribution<Action>> actionFunction = new HashMap<>();
  private final Map<Node, Map<Action, Map<Observation, Distribution<Node>>>> transitionFunction = new HashMap<>();

  public static FiniteStateController createArbitraryController(Set<Action> actions, Set<Observation> observations) {
    var builder = new FiniteStateControllerBuilder();
    Node node = new Node("N1");
    var actionDistribution = Distribution.createUniformDistribution(actions);
    builder.addNode(node).addActionSelection(node, actionDistribution);
    for (var action : actions) {
      for (var observation : observations) {
        builder.addTransition(node, action, observation, Distribution.createSingleEntryDistribution(node));
      }
    }
    return builder.createFiniteStateController();
  }

  public FiniteStateControllerBuilder addNode(String nodeString) {
    return addNode(new Node(nodeString));
  }

  public FiniteStateControllerBuilder addNode(Node node) {
    this.nodes.add(node);
    return this;
  }

  public FiniteStateControllerBuilder addNodes(Collection<Node> nodes) {
    this.nodes.addAll(nodes);
    return this;
  }

  public FiniteStateControllerBuilder addActionSelection(String nodeString, Distribution<String> actionStringDistribution) {
    var node = new Node(nodeString);
    var distributionMap = new HashMap<Action, Double>();
    for (var actionString : actionStringDistribution.keySet()) {
      var action = new Action(actionString);
      distributionMap.putIfAbsent(action, actionStringDistribution.getProbability(actionString));
    }
    try {
      Distribution<Action> actions = Distribution.of(distributionMap);
      return addActionSelection(node, actions);
    } catch (DistributionEmptyException | DistributionSumNotOneException e) {
      throw new IllegalStateException(e);
    }
  }

  public FiniteStateControllerBuilder addActionSelection(Node node, Distribution<Action> actions) {
    this.actionFunction.put(node, actions);
    return this;
  }

  public FiniteStateControllerBuilder addTransition(String nodeString, String actionString, String observationString, Distribution<String> nextNodeStringDistribution) {
    var node = new Node(nodeString);
    var action = new Action(actionString);
    var observation = new Observation(observationString);
    var distributionMap = new HashMap<Node, Double>();
    for (var nextNodeString : nextNodeStringDistribution.keySet()) {
      var nextNode = new Node(nextNodeString);
      var probability = nextNodeStringDistribution.getProbability(nextNodeString);
      distributionMap.putIfAbsent(nextNode, probability);
    }
    Distribution<Node> nextNodeDistribution;
    try {
      nextNodeDistribution = Distribution.of(distributionMap);
    } catch (DistributionEmptyException | DistributionSumNotOneException e) {
      throw new IllegalStateException(e);
    }
    return addTransition(node, action, observation, nextNodeDistribution);
  }

  public FiniteStateControllerBuilder addTransition(Node node, Action action, Observation observation, Distribution<Node> nextNodeDistribution) {
    this.transitionFunction.putIfAbsent(node, new HashMap<>());
    this.transitionFunction.get(node).putIfAbsent(action, new HashMap<>());
    this.transitionFunction.get(node).get(action).put(observation, nextNodeDistribution);
    return this;
  }

  public FiniteStateController createFiniteStateController() {
    return new FiniteStateController(nodes, actionFunction, transitionFunction);
  }
}