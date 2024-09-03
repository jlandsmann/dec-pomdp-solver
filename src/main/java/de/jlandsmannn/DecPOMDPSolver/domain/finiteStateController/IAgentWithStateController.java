package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IAgentWithStateController extends IAgent {
  FiniteStateController getController();
  void setController(FiniteStateController controller);
  Set<Node> getInitialControllerNodes();
  void setInitialControllerNodes(Set<Node> initialNodes);

  long getControllerNodeIndex();
  List<Node> getControllerNodes();

  List<Node> getFollowNodes(Node node);
  List<Action> getSelectableActions(Node node);

  double getActionSelectionProbability(Node node, Action action);
  double getNodeTransitionProbability(Node node, Action action, Observation observation, Node followNode);

  void addNode(Node node, Action action);
  void addTransition(Node node, Action action, Observation observation, Node newNode);
  void pruneNode(Node nodeToPrune, Distribution<Node> nodesToReplaceWith);

  void retainNodesAndFollower(Collection<Node> nodesToRetain);
}
