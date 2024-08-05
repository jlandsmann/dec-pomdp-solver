package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorCombinationBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This class is an abstract implementation of {@link DecPOMDP}.
 * Similar to the BasicAgent class, this class defines all common properties of a normal DecPOMDP,
 * like the transition, reward and observation function.
 * On creation, it validates all parameters on their consistency.
 * @param <AGENT>
 */
public abstract class GroundDecPOMDP<AGENT extends IAgent> extends DecPOMDP<AGENT> {
  protected final Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction;
  protected final Map<State, Map<Vector<Action>, Double>> rewardFunction;
  protected final Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction;

  public GroundDecPOMDP(List<AGENT> agents, List<State> states, double discountFactor, Distribution<State> initialBeliefState,
                        Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction,
                        Map<State, Map<Vector<Action>, Double>> rewardFunction,
                        Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction) {
    super(agents, states, discountFactor, initialBeliefState);
    this.transitionFunction = transitionFunction;
    this.rewardFunction = rewardFunction;
    this.observationFunction = observationFunction;
    validateTransitionFunction();
    validateRewardFunction();
    validateObservationFunction();
  }

  @Override
  public double getTransitionProbability(State currentState, Vector<Action> agentActions, State followState) {
    if (agentActions.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match agent count.");
    }
    return doGetTransitionProbability(currentState, agentActions, followState);
  }

  protected double doGetTransitionProbability(State currentState, Vector<Action> agentActions, State followState) {
    return Optional
      .ofNullable(transitionFunction.get(currentState))
      .map(t -> t.get(agentActions))
      .map(t -> t.getProbability(followState))
      .orElse(0D);
  }

  public double getReward(State currentState, Vector<Action> agentActions) {
    if (agentActions.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match agent count.");
    }
    return doGetReward(currentState, agentActions);
  }

  protected double doGetReward(State currentState, Vector<Action> agentActions) {
    return Optional
      .ofNullable(rewardFunction.get(currentState))
      .map(t -> t.get(agentActions))
      .orElse(0D);
  }

  @Override
  public double getObservationProbability(Vector<Action> agentActions, State followState, Vector<Observation> agentObservations) {
    if (agentActions.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match agent count.");
    } else if (agentObservations.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of observation vector doesn't match agent count.");
    }
    return doGetObservationProbability(agentActions, followState, agentObservations);
  }

  public double doGetObservationProbability(Vector<Action> agentActions, State followState, Vector<Observation> agentObservations) {
    return Optional
      .ofNullable(observationFunction.get(agentActions))
      .map(t -> t.get(followState))
      .map(t -> t.getProbability(agentObservations))
      .orElse(0D);
  }

  public List<Vector<Action>> getActionCombinations() {
    var rawCombinations = agents.stream().map(IAgent::getActions).toList();
    return VectorCombinationBuilder.listOf(rawCombinations);
  }

  public List<Vector<Observation>> getObservationCombinations() {
    var rawCombinations = agents.stream().map(IAgent::getObservations).toList();
    return VectorCombinationBuilder.listOf(rawCombinations);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GroundDecPOMDP<?> decPOMDP)) return false;
    return super.equals(decPOMDP)
      && Objects.equals(transitionFunction, decPOMDP.transitionFunction)
      && Objects.equals(rewardFunction, decPOMDP.rewardFunction)
      && Objects.equals(observationFunction, decPOMDP.observationFunction);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), transitionFunction, rewardFunction, observationFunction);
  }

  protected void validateTransitionFunction() {
    if (transitionFunction.size() != states.size()) {
      throw new IllegalArgumentException("Transition function does not match state count");
    }
    for (var state : transitionFunction.keySet()) {
      var innerMap = transitionFunction.get(state);
      for (var actionVector : innerMap.keySet()) {
        if (actionVector.size() != getAgentCount()) {
          throw new IllegalArgumentException("Some action vector of transition function does not match agent count.");
        }
      }
    }
  }

  protected void validateRewardFunction() {
    for (var state : rewardFunction.keySet()) {
      var innerMap = rewardFunction.get(state);
      for (var actionVector : innerMap.keySet()) {
        if (actionVector.size() != getAgentCount()) {
          throw new IllegalArgumentException("Some action vector of reward function does not match agent count.");
        }
      }
    }
  }

  protected void validateObservationFunction() {
    for (var actionVector : observationFunction.keySet()) {
      if (actionVector.size() != getAgentCount()) {
        throw new IllegalArgumentException("Some action vector of observation function does not match agent count.");
      } else if (observationFunction.get(actionVector).size() != states.size()) {
        throw new IllegalArgumentException("For some action vector of observation function not every state is matched." + "Action vector: " + actionVector);
      }
      var innerMap = observationFunction.get(actionVector);
      for (var state : innerMap.keySet()) {
        for (var vector : innerMap.get(state)) {
          if (vector.size() != getAgentCount()) {
            throw new IllegalArgumentException("For some action vector of observation function observations does not match agent count.");
          }
        }
      }
    }
  }

  public Map<State, Map<Vector<Action>, Distribution<State>>> getTransitionFunction() {
    return transitionFunction;
  }

  public Map<State, Map<Vector<Action>, Double>> getRewardFunction() {
    return rewardFunction;
  }

  public Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> getObservationFunction() {
    return observationFunction;
  }
}
