package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.*;

/**
 * This class helps to create a DecPOMDP step by step.
 * It does no business logic validation.
 * This is done by the DecPOMDP itself on creation.
 */
public abstract class DecPOMDPBuilder<DECPOMDP extends IDecPOMDP<AGENT>, AGENT extends IAgent, THIS extends DecPOMDPBuilder<DECPOMDP, AGENT, ?>> {
  protected final List<AGENT> agents = new ArrayList<>();
  protected final List<State> states = new ArrayList<>();
  protected final Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction = new HashMap<>();
  protected final Map<State, Map<Vector<Action>, Double>> rewardFunction = new HashMap<>();
  protected final Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction = new HashMap<>();
  protected double discountFactor;
  protected Distribution<State> initialBeliefState;

  /**
   * This creates the desired DecPOMDP, if all required information were given.
   * @return the created DecPOMDP
   */
  public abstract DECPOMDP createDecPOMDP();

  /**
   * This returns an instance of a builder for the kind of agent for this kind of DecPOMDP.
   * @return an agent builder
   */
  public abstract AgentBuilder<AGENT, ?> getAgentBuilder();

  /**
   * This adds multiple agents to the DecPOMDP
   * @param agents a collection of agents to add
   * @return the current instance
   */
  public THIS addAgents(Collection<? extends IAgent> agents) {
    agents.forEach(this::addAgent);
    return (THIS) this;
  }

  /**
   * This adds a single agent to the DecPOMDP
   * @param agent an agent to add
   * @return the current instance
   */
  public THIS addAgent(IAgent agent) {
    this.agents.remove((AGENT) agent);
    this.agents.add((AGENT) agent);
    return (THIS) this;
  }

  /**
   * This adds a states to the DecPOMDP, identified by name
   * @param stateString the name of the state to add
   * @return the current instance
   */
  public THIS addState(String stateString) {
    this.addState(State.from(stateString));
    return (THIS) this;
  }

  /**
   * This adds a states to the DecPOMDP
   * @param state the state to add
   * @return the current instance
   */
  public THIS addState(State state) {
    this.states.remove(state);
    this.states.add(state);
    return (THIS) this;
  }

  /**
   * This adds multiple states to the DecPOMDP
   * @param states the states to add
   * @return the current instance
   */
  public THIS addStates(Collection<State> states) {
    states.forEach(this::addState);
    return (THIS) this;
  }

  /**
   * This adds a transition rule to the DecPOMDP
   * @param state the state the transition starts in
   * @param actions the action vector required for transition
   * @param targetState the state the transition ends in
   * @return the current instance
   */
  public THIS addTransition(State state, Vector<Action> actions, State targetState) {
    var beliefState = Distribution.createSingleEntryDistribution(targetState);
    return addTransition(state, actions, beliefState);
  }

  /**
   * This adds a transition rule to the DecPOMDP
   * @param state the state the transition starts in
   * @param actions the action vector required for transition
   * @param beliefState the belief state the transition ends in
   * @return the current instance
   */
  public THIS addTransition(State state, Vector<Action> actions, Distribution<State> beliefState) {
    this.transitionFunction.putIfAbsent(state, new HashMap<>());
    this.transitionFunction.get(state).put(actions, beliefState);
    return (THIS) this;
  }

  /**
   * This adds a reward rule to the DecPOMDP
   * @param state the state the reward is earned in
   * @param actions the action vector required for reward
   * @return the current instance
   */
  public THIS addReward(State state, Vector<Action> actions, double reward) {
    this.rewardFunction.putIfAbsent(state, new HashMap<>());
    this.rewardFunction.get(state).put(actions, reward);
    return (THIS) this;
  }

  /**
   * This adds an observation rule to the DecPOMDP
   * @param actions the action vector performed
   * @param targetState the state the transition ends in
   * @param observations the distributions of observations made
   * @return the current instance
   */
  public THIS addObservation(Vector<Action> actions, State targetState, Distribution<Vector<Observation>> observations) {
    this.observationFunction.putIfAbsent(actions, new HashMap<>());
    this.observationFunction.get(actions).put(targetState, observations);
    return (THIS) this;
  }

  /**
   * This defines the initial belief state for the DecPOMDP.
   * @param initialBeliefState the initial state distribution
   * @return the current instance
   */
  public THIS setInitialBeliefState(Distribution<State> initialBeliefState) {
    this.initialBeliefState = initialBeliefState;
    return (THIS) this;
  }

  public List<State> getStates() {
    return states;
  }

  public List<AGENT> getAgents() {
    return agents;
  }

  public double getDiscountFactor() {
    return discountFactor;
  }

  public THIS setDiscountFactor(double discountFactor) {
    this.discountFactor = discountFactor;
    return (THIS) this;
  }
}