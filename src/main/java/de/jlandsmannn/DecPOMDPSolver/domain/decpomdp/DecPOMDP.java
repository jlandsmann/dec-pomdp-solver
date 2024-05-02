package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.*;
import java.util.stream.Collectors;

public abstract class DecPOMDP<AGENT extends Agent> {
  protected final int agentCount;
  protected final int stateCount;
  protected final List<AGENT> agents;
  protected final List<State> states;
  protected final double discountFactor;
  private final Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction;
  private final Map<State, Map<Vector<Action>, Double>> rewardFunction;
  private final Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction;

  public DecPOMDP(List<AGENT> agents, List<State> states, double discountFactor,
                  Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction,
                  Map<State, Map<Vector<Action>, Double>> rewardFunction,
                  Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction) {
    this.agentCount = agents.size();
    this.agents = agents;
    this.stateCount = states.size();
    this.states = states;
    this.discountFactor = discountFactor;
    this.transitionFunction = transitionFunction;
    this.rewardFunction = rewardFunction;
    this.observationFunction = observationFunction;
    validateDiscountFactor(discountFactor);
    validateTransitionFunction();
    validateRewardFunction();
    validateObservationFunction();
  }

  public List<State> getStates() {
    return states;
  }

  public List<AGENT> getAgents() {
    return agents;
  }

  public Distribution<State> getTransition(Distribution<State> currentState, Vector<Action> agentActions) {
    Map<Distribution<State>, Double> probabilities = new HashMap<>();
    for (State state : currentState.keySet()) {
      var probability = currentState.getProbability(state);
      var nextState = getTransition(state, agentActions);
      probabilities.put(nextState, probability);
    }
    return Distribution.createWeightedDistribution(probabilities);
  }

  public Distribution<State> getTransition(State currentState, Vector<Action> agentActions) {
    return transitionFunction.get(currentState).get(agentActions);
  }

  public double getReward(Distribution<State> currentState, Vector<Action> agentActions) {
    var reward = 0D;
    for (State state : currentState.keySet()) {
      var probability = currentState.getProbability(state);
      var rewardForState = getReward(state, agentActions);
      reward += probability * rewardForState;
    }
    return reward;
  }

  public double getReward(State currentState, Vector<Action> agentActions) {
    return rewardFunction.get(currentState).get(agentActions);
  }

  public Distribution<Vector<Observation>> getObservations(Vector<Action> agentActions, Distribution<State> nextBeliefState) {
    Map<Distribution<Vector<Observation>>, Double> map = nextBeliefState.entrySet().stream()
      .map(entry -> {
        var state = entry.getKey();
        var probability = entry.getValue();
        var distribution = getObservations(agentActions, state);
        return Map.entry(distribution, probability);
      })
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return Distribution.createWeightedDistribution(map);
  }

  public Distribution<Vector<Observation>> getObservations(Vector<Action> agentActions, State nextState) {
    return observationFunction.get(agentActions).get(nextState);
  }

  public double getDiscountFactor() {
    return discountFactor;
  }

  public abstract double getValue(Distribution<State> beliefSate);

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DecPOMDP<?> decPOMDP)) return false;
    return Double.compare(getDiscountFactor(), decPOMDP.getDiscountFactor()) == 0
      && Objects.equals(getAgents(), decPOMDP.getAgents())
      && Objects.equals(getStates(), decPOMDP.getStates())
      && Objects.equals(transitionFunction, decPOMDP.transitionFunction)
      && Objects.equals(rewardFunction, decPOMDP.rewardFunction)
      && Objects.equals(observationFunction, decPOMDP.observationFunction);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
      getAgents(),
      getStates(),
      getDiscountFactor(),
      transitionFunction, 
      rewardFunction,
      observationFunction);
  }

  private void validateDiscountFactor(double discountFactor) {
    if (discountFactor <= 0 || 1 <= discountFactor) {
      throw new IllegalArgumentException("discountFactor must be a positive number between 0 and 1");
    }
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
        for (var vector : innerMap.get(state)) {
          if (vector.size() != agentCount) {
            throw new IllegalArgumentException("For some action vector of observation function observations does not match agent count.");
          }
        }
      }
    }
  }
}
