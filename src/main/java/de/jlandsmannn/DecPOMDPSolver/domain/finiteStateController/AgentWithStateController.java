package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AgentWithStateController extends Agent {

  protected final FiniteStateController controller;

  public AgentWithStateController(String name, Set<Action> actions, Set<Observation> observations, FiniteStateController controller) {
    super(name, actions, observations);
    this.controller = controller;
  }

  public static AgentWithStateController createArbitraryAgent(String name, int numberOfActions, int numberOfObservations) {
    Set<Action> actions = IntStream.range(1, numberOfActions).mapToObj(i -> new Action(name + "-A" + i)).collect(Collectors.toSet());
    Set<Observation> observations = IntStream.range(1, numberOfObservations).mapToObj(i -> new Observation(name + "-O" + i)).collect(Collectors.toSet());
    var controller = FiniteStateControllerBuilder.createArbitraryController(actions, observations);
    return new AgentWithStateController(name, actions, observations, controller);
  }

  public long getControllerNodeIndex() {
    return controller.getNodeIndex();
  }

  public List<Node> getControllerNodes() {
    return controller.getNodes();
  }

  public Distribution<Action> getAction(Node node) {
    return controller.getAction(node);
  }

  public Distribution<Node> getTransition(Node node, Action action, Observation observation) {
    return controller.getFollowNode(node, action, observation);
  }

  public double getActionProbability(Node node, Action action) {
    return controller.getAction(node).getProbability(action);
  }

  public double getNodeTransitionProbability(Node node, Action action, Observation observation, Node newNode) {
    return controller.getFollowNode(node, action, observation).getProbability(newNode);
  }

  public void addNode(Node node, Distribution<Action> actionDistribution) {
    controller.addNode(node, actionDistribution);
  }

  public void addTransition(Node node, Action action, Observation observation, Distribution<Node> newNode) {
    controller.addTransition(node, action, observation, newNode);
  }

  public void pruneNodes(Set<Node> nodesToPrune, Distribution<Node> nodesToReplaceWith) {
    controller.pruneNodes(nodesToPrune, nodesToReplaceWith);
  }

  public void pruneNode(Node nodeToPrune, Distribution<Node> nodesToReplaceWith) {
    controller.pruneNode(nodeToPrune, nodesToReplaceWith);
  }
}
