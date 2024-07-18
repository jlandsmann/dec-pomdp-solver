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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class IsomorphicDecPOMDPWithStateController
  extends IsomorphicDecPOMDP<IsomorphicAgentWithStateController>
  implements IDecPOMDPWithStateController<IsomorphicAgentWithStateController> {
  private static final int INITIAL_VALUE_FUNCTION_SIZE_PER_STATE = 200_000;
  private static final float VALUE_FUNCTION_LOAD_FACTOR = 0.9F;

  private final Map<State, Map<Vector<Node>, Double>> preCalculatedValueFunction = new ConcurrentHashMap<>(getStates().size() + 2, 1F);

  protected IsomorphicDecPOMDPWithStateController(List<IsomorphicAgentWithStateController> agents,
                                                  List<State> states,
                                                  double discountFactor,
                                                  Distribution<State> initialBeliefState,
                                                  Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction,
                                                  Map<State, Map<Vector<Action>, Double>> rewardFunction,
                                                  Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction) {
    super(agents, states, discountFactor, initialBeliefState, transitionFunction, rewardFunction, observationFunction);
  }

  @Override
  public Vector<Node> getBestNodeCombinationFor(Distribution<State> beliefState) {
    return getNodeCombinations().stream()
      .max(Comparator.comparingDouble(nodeCombination -> getValue(beliefState, nodeCombination)))
      .orElseThrow();
  }

  @Override
  public double getValue(Distribution<State> beliefState) {
    return getNodeCombinations().stream()
      .mapToDouble(nodeCombination -> getValue(beliefState, nodeCombination))
      .max()
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
  public double getActionVectorProbability(Vector<Node> nodes, Vector<Action> actions) {
    var offset = 0;
    var probability = 1D;
    for (int i = 0; i < getAgents().size(); i++) {
      var agent = getAgents().get(i);
      for (int j = 0; j < agent.getPartitionSize(); j++) {
        var node = nodes.get(offset + j);
        var action = actions.get(offset + j);
        probability *= agent.getActionSelectionProbability(node, action);
      }
      offset += agent.getPartitionSize();
    }
    return probability;
  }

  @Override
  public double getNodeTransitionProbability(Vector<Node> nodes, Vector<Action> actions, Vector<Observation> observations, Vector<Node> newNodes) {
    var offset = 0;
    var probability = 1D;
    for (int i = 0; i < getAgents().size(); i++) {
      var agent = getAgents().get(i);
      for (int j = 0; j < agent.getPartitionSize(); j++) {
        var node = nodes.get(offset + j);
        var action = actions.get(offset + j);
        var observation = observations.get(offset + j);
        var newNode = newNodes.get(offset + j);
        probability *= agent.getNodeTransitionProbability(node, action, observation, newNode);
      }
      offset += agent.getPartitionSize();
    }
    return probability;
  }

  @Override
  public List<Vector<Node>> getNodeCombinations() {
    return getAgents().stream()
      .map(agent -> HistogramBuilder.listOf(agent.getControllerNodes(), agent.getPartitionSize()))
      .collect(CombinationCollectors.toCombinationVectors())
      .map(vector -> vector.stream()
        .flatMap(histogram -> histogram.toList().stream())
        .collect(CustomCollectors.toVector())
      )
      .toList();
  }
}
