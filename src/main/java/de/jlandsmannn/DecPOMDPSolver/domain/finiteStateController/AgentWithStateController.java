package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This is an extension of the base agent,
 * which uses {@link FiniteStateController} to represent its own policy.
 * This agent is used by {@link DecPOMDPWithStateController}.
 */
public class AgentWithStateController extends Agent {

  protected final FiniteStateController controller;
  protected Set<Node> initialControllerNodes = Set.of();

  /**
   * Default constructor with name, actions, observations and controller.
   *
   * @param name         The name of this agent
   * @param actions      The actions of this agent
   * @param observations The observations of this agent
   * @param controller   The controller of this agent
   */
  public AgentWithStateController(String name, List<Action> actions, List<Observation> observations, FiniteStateController controller) {
    super(name, actions, observations);
    this.controller = controller;
  }

  /**
   * Getter for {@link AgentWithStateController#initialControllerNodes}
   *
   * @return the nodes of controller that provide most utility at the initial belief state(s)
   */
  public Set<Node> getInitialControllerNodes() {
    return initialControllerNodes;
  }

  /**
   * Setter for {@link AgentWithStateController#initialControllerNodes}
   *
   * @param initialNodes the nodes of controller that provide most utility at the initial belief state(s)
   */
  public void setInitialControllerNodes(Set<Node> initialNodes) {
    initialNodes.retainAll(getControllerNodes());
    this.initialControllerNodes = initialNodes;
  }

  /**
   * {@link FiniteStateController#getNodeIndex()}
   */
  public long getControllerNodeIndex() {
    return controller.getNodeIndex();
  }

  /**
   * {@link FiniteStateController#getNodes()}
   */
  public List<Node> getControllerNodes() {
    return controller.getNodes();
  }

  /**
   * {@link FiniteStateController#getActionSelection(Node)}
   */
  public Distribution<Action> getActionSelection(Node node) {
    return controller.getActionSelection(node);
  }

  /**
   * {@link FiniteStateController#getTransition(Node, Action, Observation)}
   *
   * @param node        the node from where the transition starts
   * @param action      the action to be taken when in node
   * @param observation the observation observed after taking action
   * @param followNode  the node to land in
   * @return the probability of the described transition
   */
  public double getNodeTransitionProbability(Node node, Action action, Observation observation, Node followNode) {
    return getNodeTransition(node, action, observation)
      .map(transition -> transition.getProbability(followNode))
      .orElse(0D);
  }

  /**
   * {@link FiniteStateController#getTransition(Node, Action, Observation)}
   *
   * @param node        the node from where the transition starts
   * @param action      the action to be taken when in node
   * @param observation the observation observed after taking action
   * @return the distribution of nodes to land in
   */
  public Optional<Distribution<Node>> getNodeTransition(Node node, Action action, Observation observation) {
    return Optional.ofNullable(controller.getTransition(node, action, observation));
  }

  /**
   * {@link FiniteStateController#addNode(Node, Action)}
   */
  public void addNode(Node node, Action action) {
    controller.addNode(node, action);
  }

  /**
   * {@link FiniteStateController#addTransition(Node, Action, Observation, Node)}
   */
  public void addTransition(Node node, Action action, Observation observation, Node newNode) {
    controller.addTransition(node, action, observation, newNode);
  }

  /**
   * {@link FiniteStateController#pruneNode(Node, Distribution<Node>)}
   */
  public void pruneNode(Node nodeToPrune, Distribution<Node> nodesToReplaceWith) {
    controller.pruneNode(nodeToPrune, nodesToReplaceWith);
  }
}
