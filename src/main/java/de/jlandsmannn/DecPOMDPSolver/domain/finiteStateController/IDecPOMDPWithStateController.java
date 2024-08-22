package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IDecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.List;

public interface IDecPOMDPWithStateController<AGENT extends IAgentWithStateController> extends IDecPOMDP<AGENT> {
  Vector<Node> getBestNodeCombinationFor(Distribution<State> beliefState);

  /**
   * It returns the precalculated value, the expected sum of rewards,
   * based on the current policies and the given belief state and the given nodes.
   * If the value was not set previously, it returns 0.
   *
   * @param beliefState the belief state to check the value for
   * @param nodes       the vector of nodes to check the value for
   * @return the expected sum of rewards
   * @throws IllegalArgumentException if the vector of nodes has different size as the DecPOMDP has agents
   */
  double getValue(Distribution<State> beliefState, Vector<Node> nodes);

  /**
   * It returns the precalculated value, the expected sum of rewards,
   * based on the current policies and the given state and the given nodes.
   * If the value was not set previously, it returns 0.
   *
   * @param state the state to check the value for
   * @param nodes the vector of nodes to check the value for
   * @return the expected sum of rewards
   * @throws IllegalArgumentException if the vector of nodes has different size as the DecPOMDP has agents
   */
  double getValue(State state, Vector<Node> nodes);

  /**
   * It checks if for the given state and vector of nodes a value is set.
   *
   * @param state the state to check for
   * @param nodes the vector of nodes to check for
   * @return whether a value is set or not
   * @throws IllegalArgumentException if the vector of nodes has different size as the DecPOMDP has agents
   */
  boolean hasValue(State state, Vector<Node> nodes);

  /**
   * Sets the value for the given state and nodes.
   *
   * @param state the state to set the value for
   * @param nodes the vector of nodes to set the value for
   * @param value the value to set
   * @throws IllegalArgumentException if the vector of nodes has different size as the DecPOMDP has agents
   */
  void setValue(State state, Vector<Node> nodes, double value);

  /**
   * Returns the probability to choose actions when the agents are in the nodes defined by nodes.
   *
   * @param nodes   the vector of nodes to start from
   * @param actions the vector of actions to check for
   * @return the probability of choosing actions when in nodes
   * @throws IllegalArgumentException if the vector of nodes or the vector of actions has different size as the DecPOMDP has agents
   */
  double getActionVectorProbability(Vector<Node> nodes, Vector<Action> actions);

  /**
   * Returns the probability to transit starting from the given nodes,
   * choosing the given actions, observing the given observations
   * and ending in the given new nodes.
   *
   * @param nodes        the vector of nodes to start from
   * @param actions      the vector of actions to check for
   * @param observations the vector of observations to check for
   * @param newNodes     the vector of nodes to land in
   * @return the probability of the transition
   * @throws IllegalArgumentException if one of the vectors has different size as the DecPOMDP has agents
   */
  double getNodeTransitionProbability(Vector<Node> nodes, Vector<Action> actions, Vector<Observation> observations, Vector<Node> newNodes);

  List<Vector<Node>> getNodeCombinations();
  List<Vector<Node>> getNodeCombinations(Vector<Node> nodeVector);
  List<Vector<Action>> getActionCombinations(Vector<Node> nodeVector);
}
