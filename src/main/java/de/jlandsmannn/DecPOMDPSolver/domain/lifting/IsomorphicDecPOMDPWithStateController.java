package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.*;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.*;
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
  public void clearValueFunction() {
    preCalculatedValueFunction.clear();
  }

  @Override
  public double getActionVectorProbability(Vector<Node> nodes, Vector<Action> actions) {
    if (nodes.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Number of nodes does not match total number of agents");
    } else if (actions.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Number of actions does not match total number of agents");
    }
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
    if (nodes.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Number of nodes does not match total number of agents");
    } else if (actions.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Number of actions does not match total number of agents");
    } else if (observations.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Number of observations does not match total number of agents");
    } else if (newNodes.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Number of newNodes does not match total number of agents");
    }
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
        .flatMap(h -> h.toList().stream())
        .collect(CustomCollectors.toVector())
      )
      .toList();
  }

  @Override
  public List<Vector<Node>> getNodeCombinations(Vector<Node> nodeVector) {
    var rawNodeCombinations = new ArrayList<List<Node>>();
    var offset = 0;
    for (int i = 0; i < getAgents().size(); i++) {
      var agent = getAgents().get(i);
      for (int j = 0; j < agent.getPartitionSize(); j++) {
        var node = nodeVector.get(offset + j);
        var actions = agent.getFollowNodes(node);
        rawNodeCombinations.add(actions);
      }
      offset += agent.getPartitionSize();
    }
    return rawNodeCombinations
      .stream()
      .collect(CombinationCollectors.toCombinationVectors())
      .toList();
  }

  @Override
  public List<Vector<Action>> getActionCombinations(Vector<Node> nodeVector) {
    var rawActionCombinations = new ArrayList<List<Action>>();
    var offset = 0;
    for (int i = 0; i < getAgents().size(); i++) {
      var agent = getAgents().get(i);
      for (int j = 0; j < agent.getPartitionSize(); j++) {
        var node = nodeVector.get(offset + j);
        var actions = agent.getSelectableActions(node);
        rawActionCombinations.add(actions);
      }
      offset += agent.getPartitionSize();
    }
    return rawActionCombinations
      .stream()
      .collect(CombinationCollectors.toCombinationVectors())
      .toList();
  }
}
