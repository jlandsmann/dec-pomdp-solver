package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class LiftedDecPOMDPWithStateController<ACTION extends Histogram<Action>, OBSERVATION extends Histogram<Observation>, NODE extends Histogram<Node>>
  extends LiftedDecPOMDP<LiftedAgentWithStateController, ACTION, OBSERVATION>
  implements IDecPOMDPWithStateController<LiftedAgentWithStateController, ACTION, OBSERVATION, NODE> {

  private static final int INITIAL_VALUE_FUNCTION_SIZE_PER_STATE = 200_000;
  private static final float VALUE_FUNCTION_LOAD_FACTOR = 0.9F;

  private final Map<State, Map<Vector<NODE>, Double>> preCalculatedValueFunction = new ConcurrentHashMap<>(getStates().size() + 2, 1F);

  protected LiftedDecPOMDPWithStateController(List<LiftedAgentWithStateController> countingAgentWithStateControllers,
                                              List<State> states,
                                              double discountFactor,
                                              Distribution<State> initialBeliefState,
                                              Map<State, Map<Vector<ACTION>, Distribution<State>>> transitionFunction,
                                              Map<State, Map<Vector<ACTION>, Double>> rewardFunction,
                                              Map<Vector<ACTION>, Map<State, Distribution<Vector<OBSERVATION>>>> observationFunction) {
    super(countingAgentWithStateControllers, states, discountFactor, initialBeliefState, transitionFunction, rewardFunction, observationFunction);
  }

  @Override
  public double getValue(Distribution<State> beliefSate) {
    return getNodeCombinations().stream()
      .map(nodeCombination -> getValue(beliefSate, nodeCombination))
      .max(Double::compare)
      .orElse(0D);
  }

  /**
   * It returns the precalculated value, the expected sum of rewards,
   * based on the current policies and the given state and the given nodes.
   * If the value was not set previously, it returns 0.
   *
   * @param beliefState the belief state to check the value for
   * @param nodes the vector of histograms of nodes to check the value for
   * @return the expected sum of rewards
   * @throws IllegalArgumentException if the vector of nodes has different size as the DecPOMDP has agents
   */
  public double getValue(Distribution<State> beliefState, Vector<NODE> nodes) {
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

  @Override
  public Vector<NODE> getBestNodeCombinationFor(Distribution<State> beliefState) {
    return getNodeCombinations()
      .stream()
      .map(nodes -> Map.entry(nodes, getValue(beliefState, nodes)))
      .max(Comparator.comparingDouble(Map.Entry::getValue))
      .map(Map.Entry::getKey)
      .orElseThrow(IllegalStateException::new);
  }

  /**
   * It returns the precalculated value, the expected sum of rewards,
   * based on the current policies and the given state and the given nodes.
   * If the value was not set previously, it returns 0.
   *
   * @param state the state to check the value for
   * @param nodes the vector of histograms of nodes to check the value for
   * @return the expected sum of rewards
   * @throws IllegalArgumentException if the vector of nodes has different size as the DecPOMDP has agents
   */
  public double getValue(State state, Vector<NODE> nodes) {
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
   * @param nodes the vector of histograms of nodes to check for
   * @return whether a value is set or not
   * @throws IllegalArgumentException if the vector of nodes has different size as the DecPOMDP has agents
   */
  public boolean hasValue(State state, Vector<NODE> nodes) {
    if (nodes.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of node vector doesn't match agent count.");
    }
    return preCalculatedValueFunction.getOrDefault(state, Map.of()).containsKey(nodes);
  }

  /**
   * Sets the value for the given state and nodes.
   *
   * @param state the state to set the value for
   * @param nodes the vector of histograms of nodes to set the value for
   * @param value the value to set
   * @throws IllegalArgumentException if the vector of nodes has different size as the DecPOMDP has agents
   */
  public void setValue(State state, Vector<NODE> nodes, double value) {
    if (nodes.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of node vector doesn't match agent count.");
    }
    preCalculatedValueFunction.putIfAbsent(state, new ConcurrentHashMap<>(INITIAL_VALUE_FUNCTION_SIZE_PER_STATE, VALUE_FUNCTION_LOAD_FACTOR));
    preCalculatedValueFunction.get(state).put(nodes, value);
  }

  public abstract List<Vector<NODE>> getNodeCombinations();
}
