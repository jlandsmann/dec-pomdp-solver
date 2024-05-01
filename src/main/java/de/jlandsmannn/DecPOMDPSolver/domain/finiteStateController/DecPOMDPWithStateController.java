package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorStreamBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class DecPOMDPWithStateController extends DecPOMDP<AgentWithStateController> {
  private final Map<State, Map<Vector<Node>, Double>> preCalculatedValueFunction = new HashMap<>();

  public DecPOMDPWithStateController(List<AgentWithStateController> agents,
                                     Set<State> states,
                                     double discountFactor,
                                     Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction,
                                     Map<State, Map<Vector<Action>, Double>> rewardFunction,
                                     Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction) {
    super(agents, states, discountFactor, transitionFunction, rewardFunction, observationFunction);
  }

  public double getValue(Distribution<State> beliefState) {
    var nodeCombinations = agents.stream().map(AgentWithStateController::getControllerNodes).toList();
    Stream<Vector<Node>> stream = VectorStreamBuilder.forEachCombination(nodeCombinations);
    return stream
      .map(nodes -> getValue(beliefState, nodes))
      .reduce(Double::max)
      .orElse(0D);
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
    var preCalculatedValuesForState = preCalculatedValueFunction.get(state);
    if (preCalculatedValuesForState == null) return 0D;
    return preCalculatedValuesForState.getOrDefault(nodes, 0D);
  }

  private double getActionVectorProbability(Vector<Node> nodes, Vector<Action> actions) {
    var probability = 0D;
    for (int i = 0; i < actions.size(); i++) {
      var agent = agents.get(i);
      var node = nodes.get(i);
      var action = actions.get(i);
      probability += agent.getAction(node).getProbability(action);
    }
    return probability;
  }

  private double getStateTransitionProbability(State state, Vector<Action> actions, Vector<Observation> observations, State newState) {
    var stateProbability = getTransition(state, actions).getProbability(newState);
    var observationProbability = getObservations(actions, newState).getProbability(observations);
    return stateProbability * observationProbability;
  }

  private double getNodeTransitionProbability(Vector<Node> nodes, Vector<Action> actions, Vector<Observation> observations, Vector<Node> newNodes) {
    var probability = 0D;
    for (int i = 0; i < nodes.size(); i++) {
      var agent = agents.get(i);
      var node = nodes.get(i);
      var action = actions.get(i);
      var observation = observations.get(i);
      var newNode = newNodes.get(i);
      probability += agent.getTransition(node, action, observation).getProbability(newNode);
    }
    return probability;
  }
}
