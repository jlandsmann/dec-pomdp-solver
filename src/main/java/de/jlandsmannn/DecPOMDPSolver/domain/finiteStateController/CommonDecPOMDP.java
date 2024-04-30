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

public class CommonDecPOMDP extends DecPOMDP<AgentWithStateController> {
  private final Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction;
  private final Map<State, Map<Vector<Action>, Double>> rewardFunction;
  private final Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction;
  private final Map<State, Map<Vector<Node>, Double>> preCalculatedValueFunction = new HashMap<>();

  public CommonDecPOMDP(List<AgentWithStateController> agents,
                        Set<State> states,
                        double discountFactor,
                        Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction,
                        Map<State, Map<Vector<Action>, Double>> rewardFunction,
                        Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction) {
    super(agents, states, discountFactor);
    this.transitionFunction = transitionFunction;
    this.rewardFunction = rewardFunction;
    this.observationFunction = observationFunction;

    validateTransitionFunction();
    validateRewardFunction();
    validateObservationFunction();
  }

  @Override
  public Distribution<State> getTransition(State currentState, Vector<Action> agentActions) {
    return transitionFunction.get(currentState).get(agentActions);
  }

  @Override
  public double getReward(State currentState, Vector<Action> agentActions) {
    return rewardFunction.get(currentState).get(agentActions);
  }

  @Override
  public Distribution<Vector<Observation>> getObservations(Vector<Action> agentActions, State nextState) {
    return observationFunction.get(agentActions).get(nextState);
  }

  @Override
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
    var stateSelected = preCalculatedValueFunction.get(state);
    if (stateSelected == null) return 0D;
    return stateSelected.getOrDefault(nodes, 0D);
  }

  private double getActionVectorProbability(Vector<Node> nodes, Vector<Action> actions) {
    var probability = 0D;
    for (int i = 0; i < actions.size(); i++) {
      var agent = agents.get(i);
      var node = nodes.get(i);
      var action = actions.get(i);
      probability += agent.getActionProbability(node, action);
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
      probability += agent.getNodeTransitionProbability(node, action, observation, newNode);
    }
    return probability;
  }

  private void validateTransitionFunction() {
    if (transitionFunction.size() != stateCount) {
      throw new IllegalArgumentException("Transition function does not match state count");
    }
    for (var state : transitionFunction.keySet()) {
      var innerMap = transitionFunction.get(state);
      for (var actionVector : innerMap.keySet()) {
        if (actionVector.size() != agentCount) {
          throw new IllegalArgumentException("Some action vector of transition function does not match agent count.");
        }
      }
    }
  }

  private void validateRewardFunction() {
    for (var state : rewardFunction.keySet()) {
      var innerMap = rewardFunction.get(state);
      for (var actionVector : innerMap.keySet()) {
        if (actionVector.size() != agentCount) {
          throw new IllegalArgumentException("Some action vector of reward function does not match agent count.");
        }
      }
    }
  }

  private void validateObservationFunction() {
    for (var actionVector : observationFunction.keySet()) {
      if (actionVector.size() != agentCount) {
        throw new IllegalArgumentException("Some action vector of observation function does not match agent count.");
      } else if (observationFunction.get(actionVector).size() != stateCount) {
        throw new IllegalArgumentException("For some action vector of observation function not every state is matched." + "Action vector: " + actionVector);
      }
      var innerMap = observationFunction.get(actionVector);
      for (var state : innerMap.keySet()) {
        if (innerMap.get(state).size() != agentCount) {
          throw new IllegalArgumentException("For some action vector of observation function observations does not match agent count.");
        }
      }
    }
  }
}
