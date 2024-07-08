package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.List;
import java.util.Objects;

/**
 * This is the abstract base class representing a DecPOMDP.
 * It needs to be abstract because the determination of the value,
 * given a specific belief state, depends on the policy of the agent.
 * Furthermore, this class does not instantiate the transition-,
 * the observation- nor the reward function, to be as general as possible.
 */
public abstract class DecPOMDP<AGENT extends IAgent, ACTION, OBSERVATION> implements IDecPOMDP<AGENT, ACTION, OBSERVATION> {
  protected final List<AGENT> agents;
  protected final List<State> states;
  protected final double discountFactor;
  protected final Distribution<State> initialBeliefState;

  public DecPOMDP(List<AGENT> agents, List<State> states, double discountFactor, Distribution<State> initialBeliefState) {
    this.agents = agents;
    this.states = states;
    this.discountFactor = discountFactor;
    this.initialBeliefState = initialBeliefState;
    validateDiscountFactor();
  }
  
  public int getAgentCount() {
    return agents.size();
  }

  public List<AGENT> getAgents() {
    return agents;
  }

  public List<State> getStates() {
    return states;
  }

  public double getDiscountFactor() {
    return discountFactor;
  }

  public Distribution<State> getInitialBeliefState() {
    return initialBeliefState;
  }

  @Override
  public double getTransitionProbability(Distribution<State> currentBeliefState, Vector<ACTION> agentActions, State followState) {
    return currentBeliefState.keySet()
      .stream()
      .mapToDouble(state -> {
        double stateProbability = currentBeliefState.getProbability(state);
        double transitionProbability = getTransitionProbability(state, agentActions, followState);
        return stateProbability * transitionProbability;
      })
      .sum();
  }

  @Override
  public abstract double getTransitionProbability(State currentState, Vector<ACTION> agentActions, State followState);

  public double getReward(Distribution<State> currentBeliefState, Vector<ACTION> agentActions) {
    if (agentActions.size() != getAgentCount()) {
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

  public abstract double getReward(State currentState, Vector<ACTION> agentActions);

  public double getObservationProbability(Vector<ACTION> agentActions, Distribution<State> followBeliefState, Vector<OBSERVATION> agentObservations) {
    return followBeliefState.keySet()
      .stream()
      .mapToDouble(followState -> {
        double stateProbability = followBeliefState.getProbability(followState);
        double observationProbability = getObservationProbability(agentActions, followState, agentObservations);
        return stateProbability * observationProbability;
      })
      .sum();
  }

  @Override
  public abstract double getObservationProbability(Vector<ACTION> agentActions, State followState, Vector<OBSERVATION> agentObservations);

  public double getValue() {
    return getValue(initialBeliefState);
  }

  public abstract double getValue(Distribution<State> beliefSate);

  public abstract List<Vector<ACTION>> getActionCombinations();

  public abstract List<Vector<OBSERVATION>> getObservationCombinations();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DecPOMDP<?, ?, ?> decPOMDP)) return false;
    return Double.compare(getDiscountFactor(), decPOMDP.getDiscountFactor()) == 0
      && Objects.equals(getAgents(), decPOMDP.getAgents())
      && Objects.equals(getStates(), decPOMDP.getStates());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
      getAgents(),
      getStates(),
      getDiscountFactor());
  }

  protected void validateDiscountFactor() {
    if (discountFactor < 0 || 1 < discountFactor) {
      throw new IllegalArgumentException("discountFactor must be a positive number between 0 and 1.");
    }
  }
}
