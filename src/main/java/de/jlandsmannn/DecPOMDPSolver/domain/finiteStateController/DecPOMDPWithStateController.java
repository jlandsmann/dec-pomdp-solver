package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorCombinationBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DecPOMDPWithStateController extends DecPOMDP<AgentWithStateController> {
  private final Map<State, Map<Vector<Node>, Double>> preCalculatedValueFunction = new ConcurrentHashMap<>();

  public DecPOMDPWithStateController(List<AgentWithStateController> agents,
                                     List<State> states,
                                     double discountFactor,
                                     Distribution<State> initialBeliefState,
                                     Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction,
                                     Map<State, Map<Vector<Action>, Double>> rewardFunction,
                                     Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction) {
    super(agents, states, discountFactor, initialBeliefState, transitionFunction, rewardFunction, observationFunction);
  }

  public double getValue(Distribution<State> beliefState) {
    var nodeCombinations = agents.stream().map(AgentWithStateController::getControllerNodes).toList();
    return VectorCombinationBuilder.streamOf(nodeCombinations)
      .map(nodes -> getValue(beliefState, nodes))
      .reduce(Double::max)
      .orElse(0D);
  }

  public Vector<Node> getBestNodeCombinationFor(Distribution<State> beliefState) {
    var nodeCombinations = agents.stream().map(AgentWithStateController::getControllerNodes).toList();
    return VectorCombinationBuilder.streamOf(nodeCombinations)
      .map(nodes -> Map.entry(nodes, getValue(beliefState, nodes)))
      .max(Map.Entry.comparingByValue(Double::compareTo))
      .map(Map.Entry::getKey)
      .orElseThrow(IllegalStateException::new);
  }

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

  public double getValue(State state, Vector<Node> nodes) {
    return getOptionalValue(state, nodes).orElse(0D);

  }

  public Optional<Double> getOptionalValue(State state, Vector<Node> nodes) {
    var preCalculatedValuesForState = preCalculatedValueFunction.getOrDefault(state, Map.of());
    var value = preCalculatedValuesForState.get(nodes);
    return Optional.ofNullable(value);
  }

  public void setValue(State state, Vector<Node> nodes, double value) {
    preCalculatedValueFunction.putIfAbsent(state, new ConcurrentHashMap<>());
    preCalculatedValueFunction.get(state).put(nodes, value);
  }

  public double getActionVectorProbability(Vector<Node> nodes, Vector<Action> actions) {
    var probability = 1D;
    for (int i = 0; i < actions.size(); i++) {
      var agent = agents.get(i);
      var node = nodes.get(i);
      var action = actions.get(i);
      probability *= agent.getActionSelection(node).getProbability(action);
    }
    return probability;
  }

  public double getNodeTransitionProbability(Vector<Node> nodes, Vector<Action> actions, Vector<Observation> observations, Vector<Node> newNodes) {
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

  public void retainNodesFromValueFunction(Set<Node> nodes) {
    for (var state : states) {
      var values = preCalculatedValueFunction.getOrDefault(state, Map.of());
      for (var nodeVector : values.keySet()) {
        if (!nodes.containsAll(nodeVector.toSet())) values.remove(nodeVector);
      }
    }
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
