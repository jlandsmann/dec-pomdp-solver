
package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.CombinationCollectors;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Histogram;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.tuple.Tuples;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class CountingDecPOMDPWithStateController extends CountingDecPOMDP<CountingAgentWithStateController> implements IDecPOMDPWithStateController<CountingAgentWithStateController> {
  private static final int INITIAL_VALUE_FUNCTION_SIZE_PER_STATE = 200_000;
  private static final float VALUE_FUNCTION_LOAD_FACTOR = 0.9F;

  private final Map<State, Map<Vector<Node>, Double>> preCalculatedValueFunction = new ConcurrentHashMap<>(getStates().size() + 2, 1F);

  public CountingDecPOMDPWithStateController(List<CountingAgentWithStateController> agents,
                                             List<State> states,
                                             double discountFactor,
                                             Distribution<State> initialBeliefState,
                                             Map<State, Map<Histogram<Action>, Distribution<State>>> transitionFunction,
                                             Map<State, Map<Histogram<Action>, Double>> rewardFunction,
                                             Map<Histogram<Action>, Map<State, Distribution<Histogram<Observation>>>> observationFunction) {
    super(agents, states, discountFactor, initialBeliefState, transitionFunction, rewardFunction, observationFunction);
  }

  @Override
  public double getValue(Distribution<State> beliefSate) {
    return getNodeCombinations().stream()
      .map(nodes -> getValue(beliefSate, nodes))
      .max(Double::compare)
      .orElse(0D);
  }

  @Override
  public double getValue(Distribution<State> beliefState, Vector<Node> nodes) {
    return beliefState.keySet().stream()
      .mapToDouble(state -> {
        var stateProbability = beliefState.getProbability(state);
        var value = getValue(state, nodes);
        return stateProbability * value;
      })
      .sum();
  }

  @Override
  public double getValue(State state, Vector<Node> nodes) {
    return Optional
      .ofNullable(preCalculatedValueFunction.get(state))
      .map(s -> s.get(nodes))
      .orElse(0D);
  }

  @Override
  public boolean hasValue(State state, Vector<Node> nodes) {
    return preCalculatedValueFunction.getOrDefault(state, Map.of()).containsKey(nodes);
  }

  @Override
  public void setValue(State state, Vector<Node> nodes, double value) {
    if (nodes.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Number of nodes does not match total number of agents");
    }
    preCalculatedValueFunction.putIfAbsent(state, new ConcurrentHashMap<>(INITIAL_VALUE_FUNCTION_SIZE_PER_STATE, VALUE_FUNCTION_LOAD_FACTOR));
    preCalculatedValueFunction.get(state).put(nodes, value);
  }

  @Override
  public void clearValueFunction() {
    preCalculatedValueFunction.clear();
  }

  @Override
  public double getActionVectorProbability(Vector<Node> nodes, Vector<Action> actions) {
    return IntStream.range(0, nodes.size())
      .mapToObj(idx -> Tuples.of(agents.get(idx), nodes.get(idx), actions.get(idx)))
      .mapToDouble(tuple -> tuple.first().getActionSelectionProbability(tuple.second(), tuple.third()))
      .reduce((a,b) -> a * b)
      .orElse(0D)
      ;
  }

  @Override
  public double getNodeTransitionProbability(Vector<Node> nodes, Vector<Action> actions, Vector<Observation> observations, Vector<Node> newNodes) {
    return IntStream.range(0, nodes.size())
      .mapToObj(idx -> Tuples.of(agents.get(idx), nodes.get(idx), actions.get(idx), observations.get(idx), newNodes.get(idx)))
      .mapToDouble(tuple -> tuple.first().getNodeTransitionProbability(tuple.second(), tuple.third(), tuple.fourth(), tuple.fifth()))
      .reduce((a,b) -> a * b)
      .orElse(0D)
      ;
  }

  @Override
  public List<Vector<Node>> getNodeCombinations() {
    return getAgents().stream()
      .map(AgentWithStateController::getControllerNodes)
      .collect(CombinationCollectors.toCombinationVectors())
      .toList();
  }

  @Override
  public List<Vector<Node>> getNodeCombinations(Vector<Node> nodeVector) {
    return IntStream.range(0, getAgents().size())
      .mapToObj(idx -> {
        var agent = getAgents().get(idx);
        var node = nodeVector.get(idx);
        return agent.getFollowNodes(node);
      })
      .collect(CombinationCollectors.toCombinationVectors())
      .toList();
  }

  @Override
  public List<Vector<Action>> getActionCombinations(Vector<Node> nodeVector) {
    return IntStream.range(0, getAgents().size())
      .mapToObj(idx -> {
        var agent = getAgents().get(idx);
        var node = nodeVector.get(idx);
        return agent.getSelectableActions(node);
      })
      .collect(CombinationCollectors.toCombinationVectors())
      .toList();
  }
}
