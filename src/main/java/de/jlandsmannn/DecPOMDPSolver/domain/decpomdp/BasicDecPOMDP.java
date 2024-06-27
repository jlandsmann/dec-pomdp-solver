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

/**
 * This class is an abstract implementation of {@link DecPOMDP}.
 * Similar to the BasicAgent class, this class defines all common properties of a normal DecPOMDP,
 * like the transition, reward and observation function.
 * On creation, it validates all parameters on their consistency.
 * @param <AGENT>
 */
public abstract class BasicDecPOMDP<AGENT extends Agent> extends DecPOMDP<AGENT, Action, Observation> {
  protected final Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction;
  protected final Map<State, Map<Vector<Action>, Double>> rewardFunction;
  protected final Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction;

  public BasicDecPOMDP(List<AGENT> agents, List<State> states, double discountFactor, Distribution<State> initialBeliefState,
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

  public Distribution<State> getTransition(State currentState, Vector<Action> agentActions) {
    return transitionFunction.get(currentState).get(agentActions);
  }

  public double getReward(State currentState, Vector<Action> agentActions) {
    if (agentActions.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match agent count.");
    }
    return rewardFunction.get(currentState).get(agentActions);
  }

  public Distribution<Vector<Observation>> getObservations(Vector<Action> agentActions, State nextState) {
    if (agentActions.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match agent count.");
    }
    return observationFunction.get(agentActions).get(nextState);
  }

  public List<Vector<Action>> getActionCombinations() {
    var rawCombinations = agents.stream().map(Agent::getActions).toList();
    return VectorCombinationBuilder.listOf(rawCombinations);
  }

  public List<Vector<Observation>> getObservationCombinations() {
    var rawCombinations = agents.stream().map(Agent::getObservations).toList();
    return VectorCombinationBuilder.listOf(rawCombinations);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BasicDecPOMDP<?> decPOMDP)) return false;
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
}
