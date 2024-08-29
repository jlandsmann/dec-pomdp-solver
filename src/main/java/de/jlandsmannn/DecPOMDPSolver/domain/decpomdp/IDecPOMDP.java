package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.CombinationCollectors;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.List;

/**
 * This interfaces describes a decentralized partially-observable markov decision process (DecPOMDP).
 *
 * @param <AGENT> THe type of agent, that is used for the DecPOMDP
 */
public interface IDecPOMDP<AGENT extends IAgent> {

  /**
   * Returns the number of agents in this DecPOMP.
   * @return the number of agents
   */
  default int getAgentCount() {
    return getAgents().size();
  }

  /**
   * Returns the agents of this DecPOMDP.
   * @return a list of agents
   */
  List<AGENT> getAgents();

  /**
   * Returns the states of this DecPOMDP.
   * @return a list of states
   */
  List<State> getStates();

  /**
   * Returns the discount factor of this DecPOMDP.
   * @return the discount factor
   */
  double getDiscountFactor();

  /**
   * Overrides the discount factor
   * @param discountFactor the new discount factor
   */
  void setDiscountFactor(double discountFactor);

  /**
   * Returns the initial belief state for this DecPOMDP
   * @return the initial belief state
   */
  Distribution<State> getInitialBeliefState();

  /**
   * Calculates the transition probability for the given belief state, vector of actions and the given state to transition into.
   * @param currentBeliefState the belief state, from where the transition shall begin
   * @param actionVector the actions each agent performs in the current belief state
   * @param followState the state to check the transition probability for
   * @return the probability for transition
   */
  default double getTransitionProbability(Distribution<State> currentBeliefState, Vector<Action> actionVector, State followState) {
    return currentBeliefState.keySet().stream().mapToDouble(state -> {
      var stateProbability = currentBeliefState.getProbability(state);
      var transitionProbability = getTransitionProbability(state, actionVector, followState);
      return stateProbability * transitionProbability;
    }).sum();
  }

  /**
   * Calculates the transition probability for the given state, vector of actions and the given state to transition into.
   * @param currentState the state, from where the transition shall begin
   * @param actionVector the actions each agent performs in the current state
   * @param followState the state to check the transition probability for
   * @return the probability for transition
   */
  double getTransitionProbability(State currentState, Vector<Action> actionVector, State followState);

  /**
   * Calculates the expected reward for the given belief state and vector of actions.
   *
   * @param currentBeliefState the belief state to calculate the reward for
   * @param actionVector       the actions each agent performs in the current belief state
   * @return the expected reward
   */
  default double getReward(Distribution<State> currentBeliefState, Vector<Action> actionVector) {
    return currentBeliefState.keySet().stream().mapToDouble(state -> {
      var stateProbability = currentBeliefState.getProbability(state);
      var reward = getReward(state, actionVector);
      return stateProbability * reward;
    }).sum();
  }

  /**
   * Calculates the expected reward for the given state and vector of actions.
   * @param currentState the state to calculate the reward for
   * @param actionVector the actions each agent performs in the current state
   * @return the expected reward
   */
  double getReward(State currentState, Vector<Action> actionVector);

  /**
   * Calculates probability for observing the given vector of observations,
   * assuming the given vector of actions was performed, and the model will transition into the given state.
   * @param actionVector the actions each agent performs in the current state
   * @param followState the state the model shall transition into
   * @param observationVector the observations to check the probability for
   * @return the probability for the given observations
   */
  double getObservationProbability(Vector<Action> actionVector, State followState, Vector<Observation> observationVector);

  /**
   * Calculates the expected reward for the initial belief state.
   * @return the expected reward
   */
  default double getValue() {
    var initialBeliefState = getInitialBeliefState();
    return getValue(initialBeliefState);
  }

  /**
   * Calculates the expected reward for the given belief state.
   * @param beliefSate the belief state to get the expected reward from
   * @return the expected reward
   */
  double getValue(Distribution<State> beliefSate);

  /**
   * Calculates all possible action vectors.
   * @return a list of action vectors
   */
  default List<Vector<Action>> getActionVectors() {
    return this.getAgents().stream().map(IAgent::getActions).collect(CombinationCollectors.toCombinationVectors()).toList();
  }

  /**
   * Calculates all possible observation vectors.
   * @return a list of observation vectors
   */
  default List<Vector<Observation>> getObservationVectors() {
    return this.getAgents().stream().map(IAgent::getObservations).collect(CombinationCollectors.toCombinationVectors()).toList();
  }
}
