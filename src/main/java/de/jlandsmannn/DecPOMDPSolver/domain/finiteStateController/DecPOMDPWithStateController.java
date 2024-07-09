package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.GroundDecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorCombinationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This DecPOMDP class utilizes {@link AgentWithStateController} as agents
 * which use {@link FiniteStateController} to represent their policies.
 * It inherits from {@link DecPOMDP}.
 */
public class DecPOMDPWithStateController extends GroundDecPOMDP<AgentWithStateController> implements IDecPOMDPWithStateController<AgentWithStateController> {
  private static final int INITIAL_VALUE_FUNCTION_SIZE_PER_STATE = 200_000;
  private static final float VALUE_FUNCTION_LOAD_FACTOR = 0.9F;

  private final Map<State, Map<Vector<Node>, Double>> preCalculatedValueFunction = new ConcurrentHashMap<>(getStates().size() + 2, 1F);

  public DecPOMDPWithStateController(List<AgentWithStateController> agents,
                                     List<State> states,
                                     double discountFactor,
                                     Distribution<State> initialBeliefState,
                                     Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction,
                                     Map<State, Map<Vector<Action>, Double>> rewardFunction,
                                     Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction) {
    super(agents, states, discountFactor, initialBeliefState, transitionFunction, rewardFunction, observationFunction);
  }

  /**
   * Implementation of {@link DecPOMDP#getValue(Distribution)}
   * It returns the precalculated value, the expected sum of rewards,
   * based on the current policies and the given belief state.
   * It will choose those nodes that maximize this value.
   * If the value was not set previously, it returns 0.
   *
   * @param beliefState the belief state to check the value for
   * @return the expected sum of rewards
   */
  @Override
  public double getValue(Distribution<State> beliefState) {
    return getNodeCombinations()
      .stream()
      .map(nodes -> getValue(beliefState, nodes))
      .reduce(Double::max)
      .orElse(0D);
  }

  /**
   * It returns those nodes, that maximize the value, the expected sum of rewards,
   * based on the current policies and the given belief state.
   *
   * @param beliefState the belief state to check the value for
   * @return the vector of nodes to start from, that maximizes the expected sum of rewards
   */
  @Override
  public Vector<Node> getBestNodeCombinationFor(Distribution<State> beliefState) {
    return getNodeCombinations()
      .stream()
      .map(nodes -> Map.entry(nodes, getValue(beliefState, nodes)))
      .max(Map.Entry.comparingByValue(Double::compareTo))
      .map(Map.Entry::getKey)
      .orElseThrow(IllegalStateException::new);
  }

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
  public double getValue(Distribution<State> beliefState, Vector<Node> nodes) {
    return beliefState
      .entrySet()
      .stream()
      .map(entry -> {
        var state = entry.getKey();
        var probability = entry.getValue();
        var value = getValue(state, nodes);
        return probability * value;
      })
      .reduce(Double::sum)
      .orElse(0D);
  }

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
  public double getValue(State state, Vector<Node> nodes) {
    if (nodes.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of node vector doesn't match agent count.");
    }
    var preCalculatedValuesForState = preCalculatedValueFunction.getOrDefault(state, Map.of());
    return preCalculatedValuesForState.getOrDefault(nodes, 0D);
  }

  /**
   * It checks if for the given state and vector of nodes a value is set.
   *
   * @param state the state to check for
   * @param nodes the vector of nodes to check for
   * @return whether a value is set or not
   * @throws IllegalArgumentException if the vector of nodes has different size as the DecPOMDP has agents
   */
  public boolean hasValue(State state, Vector<Node> nodes) {
    if (nodes.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of node vector doesn't match agent count.");
    }
    return preCalculatedValueFunction.getOrDefault(state, Map.of()).containsKey(nodes);
  }

  /**
   * Sets the value for the given state and nodes.
   *
   * @param state the state to set the value for
   * @param nodes the vector of nodes to set the value for
   * @param value the value to set
   * @throws IllegalArgumentException if the vector of nodes has different size as the DecPOMDP has agents
   */
  public void setValue(State state, Vector<Node> nodes, double value) {
    if (nodes.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of node vector doesn't match agent count.");
    }
    preCalculatedValueFunction.putIfAbsent(state, new ConcurrentHashMap<>(INITIAL_VALUE_FUNCTION_SIZE_PER_STATE, VALUE_FUNCTION_LOAD_FACTOR));
    preCalculatedValueFunction.get(state).put(nodes, value);
  }

  /**
   * Returns the probability to choose actions when the agents are in the nodes defined by nodes.
   *
   * @param nodes   the vector of nodes to start from
   * @param actions the vector of actions to check for
   * @return the probability of choosing actions when in nodes
   * @throws IllegalArgumentException if the vector of nodes or the vector of actions has different size as the DecPOMDP has agents
   */
  public double getActionVectorProbability(Vector<Node> nodes, Vector<Action> actions) {
    if (nodes.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of node vector doesn't match agent count.");
    } else if (actions.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match agent count.");
    }
    var probability = 1D;
    for (int i = 0; i < actions.size() && probability != 0; i++) {
      var agent = agents.get(i);
      var node = nodes.get(i);
      var action = actions.get(i);
      probability *= agent.getActionSelectionProbability(node, action);
    }
    return probability;
  }

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
  public double getNodeTransitionProbability(Vector<Node> nodes, Vector<Action> actions, Vector<Observation> observations, Vector<Node> newNodes) {
    if (nodes.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of node vector doesn't match agent count.");
    } else if (actions.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match agent count.");
    } else if (observations.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of observation vector doesn't match agent count.");
    } else if (newNodes.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of newNodes vector doesn't match agent count.");
    }
    var probability = 1D;
    for (int i = 0; i < nodes.size() && probability != 0; i++) {
      var agent = agents.get(i);
      var node = nodes.get(i);
      var action = actions.get(i);
      var observation = observations.get(i);
      var newNode = newNodes.get(i);
      probability *= agent.getNodeTransitionProbability(node, action, observation, newNode);
    }
    return probability;
  }

  public List<Vector<Node>> getNodeCombinations() {
    var readonlyRawCombinations = getAgents().stream().map(AgentWithStateController::getControllerNodes).toList();
    var rawCombinations = new ArrayList<>(readonlyRawCombinations);
    return VectorCombinationBuilder.listOf(rawCombinations);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DecPOMDPWithStateController that)) return false;
    if (!super.equals(o)) return false;
    return Objects.equals(preCalculatedValueFunction, that.preCalculatedValueFunction);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), preCalculatedValueFunction);
  }
}
