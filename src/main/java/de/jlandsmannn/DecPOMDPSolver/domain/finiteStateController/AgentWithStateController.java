package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

import java.util.List;
import java.util.Set;

public class AgentWithStateController extends Agent {

  protected final FiniteStateController controller;

  public AgentWithStateController(String name, List<Action> actions, List<Observation> observations, FiniteStateController controller) {
    super(name, actions, observations);
    this.controller = controller;
  }

  public long getControllerNodeIndex() {
    return controller.getNodeIndex();
  }

  public List<Node> getControllerNodes() {
    return controller.getNodes();
  }

  public Distribution<Action> getActionSelection(Node node) {
    return controller.getActionSelection(node);
  }

  public Distribution<Node> getNodeTransition(Node node, Action action, Observation observation) {
    return controller.getTransition(node, action, observation);
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

  public void pruneNode(Node nodeToPrune) {
    controller.pruneNode(nodeToPrune);
  }
}
