package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is the abstract base class representing a DecPOMDP.
 * It needs to be abstract because the determination of the value,
 * given a specific belief state, depends on the policy of the agent.
 * Similar to the base agent class, this class defines all common properties of a DecPOMDP,
 * like the transition, reward and observation function, as well as the
 * states, agents, discountFactor and the initial belief state.
 * On creation, it validates all parameters on their consistency.
 */
public abstract class DecPOMDP<AGENT extends Agent> {
  protected final List<AGENT> agents;
  protected final List<State> states;
  protected final double discountFactor;
  protected final Distribution<State> initialBeliefState;
  private final Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction;
  private final Map<State, Map<Vector<Action>, Double>> rewardFunction;
  private final Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction;

  public DecPOMDP(List<AGENT> agents, List<State> states, double discountFactor, Distribution<State> initialBeliefState,
                  Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction,
                  Map<State, Map<Vector<Action>, Double>> rewardFunction,
                  Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction) {
    this.agents = agents;
    this.states = states;
    this.discountFactor = discountFactor;
    this.initialBeliefState = initialBeliefState;
    this.transitionFunction = transitionFunction;
    this.rewardFunction = rewardFunction;
    this.observationFunction = observationFunction;
    validateDiscountFactor();
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

  public Distribution<State> getInitialBeliefState() {
    return initialBeliefState;
  }

  public Distribution<State> getTransition(Distribution<State> currentBeliefState, Vector<Action> agentActions) {
    if (agentActions.size() != agents.size()) {
      throw new IllegalArgumentException("Length of action vector doesn't match agent count.");
    }
    Map<Distribution<State>, Double> map = currentBeliefState.entrySet().stream()
      .map(entry -> {
        var state = entry.getKey();
        var probability = entry.getValue();
        var distribution = getTransition(state, agentActions);
        return Map.entry(distribution, probability);
      })
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return Distribution.createWeightedDistribution(map);
  }

  public Distribution<State> getTransition(State currentState, Vector<Action> agentActions) {
    return transitionFunction.get(currentState).get(agentActions);
  }

  public double getReward(Distribution<State> currentBeliefState, Vector<Action> agentActions) {
    if (agentActions.size() != agents.size()) {
      throw new IllegalArgumentException("Length of action vector doesn't match agent count.");
    }
    return currentBeliefState.entrySet().stream()
      .map(entry -> {
        var state = entry.getKey();
        var probability = entry.getValue();
        var reward = getReward(state, agentActions);
        return probability * reward;
      })
      .reduce(Double::sum)
      .orElse(0D);
  }

  public double getReward(State currentState, Vector<Action> agentActions) {
    if (agentActions.size() != agents.size()) {
      throw new IllegalArgumentException("Length of action vector doesn't match agent count.");
    }
    return rewardFunction.get(currentState).get(agentActions);
  }

  public Distribution<Vector<Observation>> getObservations(Vector<Action> agentActions, Distribution<State> nextBeliefState) {
    if (agentActions.size() != agents.size()) {
      throw new IllegalArgumentException("Length of action vector doesn't match agent count.");
    }
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
    if (agentActions.size() != agents.size()) {
      throw new IllegalArgumentException("Length of action vector doesn't match agent count.");
    }
    return observationFunction.get(agentActions).get(nextState);
  }

  public double getDiscountFactor() {
    return discountFactor;
  }

  public abstract double getValue(Distribution<State> beliefSate);

  public double getValue() {
    return getValue(initialBeliefState);
  }

  public double getTransitionProbability(State state, Vector<Action> actions, Vector<Observation> observations, State newState) {
    if (actions.size() != agents.size()) {
      throw new IllegalArgumentException("Length of action vector doesn't match agent count.");
    } else if (observations.size() != agents.size()) {
      throw new IllegalArgumentException("Length of observation vector doesn't match agent count.");
    }
    var stateProbability = getTransition(state, actions).getProbability(newState);
    var observationProbability = getObservations(actions, newState).getProbability(observations);
    return stateProbability * observationProbability;
  }

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

  protected void validateDiscountFactor() {
    if (discountFactor < 0 || 1 < discountFactor) {
      throw new IllegalArgumentException("discountFactor must be a positive number between 0 and 1.");
    }
  }

  protected void validateTransitionFunction() {
    if (transitionFunction.size() != states.size()) {
      throw new IllegalArgumentException("Transition function does not match state count");
    }
    for (var state : transitionFunction.keySet()) {
      var innerMap = transitionFunction.get(state);
      for (var actionVector : innerMap.keySet()) {
        if (actionVector.size() != agents.size()) {
          throw new IllegalArgumentException("Some action vector of transition function does not match agent count.");
        }
      }
    }
  }

  protected void validateRewardFunction() {
    for (var state : rewardFunction.keySet()) {
      var innerMap = rewardFunction.get(state);
      for (var actionVector : innerMap.keySet()) {
        if (actionVector.size() != agents.size()) {
          throw new IllegalArgumentException("Some action vector of reward function does not match agent count.");
        }
      }
    }
  }

  protected void validateObservationFunction() {
    for (var actionVector : observationFunction.keySet()) {
      if (actionVector.size() != agents.size()) {
        throw new IllegalArgumentException("Some action vector of observation function does not match agent count.");
      } else if (observationFunction.get(actionVector).size() != states.size()) {
        throw new IllegalArgumentException("For some action vector of observation function not every state is matched." + "Action vector: " + actionVector);
      }
      var innerMap = observationFunction.get(actionVector);
      for (var state : innerMap.keySet()) {
        for (var vector : innerMap.get(state)) {
          if (vector.size() != agents.size()) {
            throw new IllegalArgumentException("For some action vector of observation function observations does not match agent count.");
          }
        }
      }
    }
  }
}
