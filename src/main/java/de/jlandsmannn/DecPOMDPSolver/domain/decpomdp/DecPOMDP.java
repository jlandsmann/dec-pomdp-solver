package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.List;
import java.util.Objects;

/**
 * This abstract base class represents a DecPOMDP
 * and is a partial implementation of the {@link IDecPOMDP} interface.
 * It needs to be abstract because the determination of the value,
 * given a specific belief state, depends on the policy of the agent.
 * Furthermore, this class does not instantiate the transition-,
 * the observation- nor the reward function, to be as general as possible.
 */
public abstract class DecPOMDP<AGENT extends IAgent> implements IDecPOMDP<AGENT> {
  protected final List<AGENT> agents;
  protected final List<State> states;
  protected double discountFactor;
  protected final Distribution<State> initialBeliefState;

  public DecPOMDP(List<AGENT> agents, List<State> states, double discountFactor, Distribution<State> initialBeliefState) {
    this.agents = agents;
    this.states = states;
    this.discountFactor = discountFactor;
    this.initialBeliefState = initialBeliefState;
    validateDiscountFactor();
  }

  @Override
  public List<AGENT> getAgents() {
    return agents;
  }

  @Override
  public List<State> getStates() {
    return states;
  }

  @Override
  public double getDiscountFactor() {
    return discountFactor;
  }

  @Override
  public void setDiscountFactor(double discountFactor) {
    this.discountFactor = discountFactor;
  }

  @Override
  public Distribution<State> getInitialBeliefState() {
    return initialBeliefState;
  }

  @Override
  public abstract double getTransitionProbability(State currentState, Vector<Action> actionVector, State followState);

  @Override
  public abstract double getReward(State currentState, Vector<Action> actionVector);

  @Override
  public abstract double getObservationProbability(Vector<Action> actionVector, State followState, Vector<Observation> observationVector);

  @Override
  public abstract double getValue(Distribution<State> beliefSate);

  @Override
  public abstract List<Vector<Action>> getActionVectors();

  @Override
  public abstract List<Vector<Observation>> getObservationVectors();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DecPOMDP<?> decPOMDP)) return false;
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
