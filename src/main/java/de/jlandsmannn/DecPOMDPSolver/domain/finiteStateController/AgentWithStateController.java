package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AgentWithStateController extends Agent {

  protected final FiniteStateController controller;
  protected Set<Node> initialControllerNodes;

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

  public double getNodeTransitionProbability(Node node, Action action, Observation observation, Node followNode) {
    return getNodeTransition(node, action, observation).map(t -> t.getProbability(followNode)).orElse(0D);
  }

  public Optional<Distribution<Node>> getNodeTransition(Node node, Action action, Observation observation) {
    return Optional.ofNullable(controller.getTransition(node, action, observation));
  }

  public void addNode(Node node, Action action) {
    controller.addNode(node, action);
  }

  public void addTransition(Node node, Action action, Observation observation, Node newNode) {
    controller.addTransition(node, action, observation, newNode);
  }

  public void pruneNode(Node nodeToPrune, Distribution<Node> nodesToReplaceWith) {
    controller.pruneNode(nodeToPrune, nodesToReplaceWith);
  }

  public void pruneNode(Node nodeToPrune, Node nodeToReplaceWith) {
    controller.pruneNode(nodeToPrune, nodeToReplaceWith);
  }

  public Set<Node> getInitialControllerNodes() {
    return initialControllerNodes;
  }

  public void setInitialControllerNodes(Set<Node> initialNodes) {
    initialNodes.retainAll(getControllerNodes());
    this.initialControllerNodes = initialNodes;
  }
}
