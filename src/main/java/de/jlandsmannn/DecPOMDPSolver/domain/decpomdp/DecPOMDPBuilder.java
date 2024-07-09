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

  public abstract DECPOMDP createDecPOMDP();

  public abstract AgentBuilder<AGENT, ?> getAgentBuilder();

  public THIS addAgents(Collection<? extends IAgent> agents) {
    agents.forEach(this::addAgent);
    return (THIS) this;
  }

  public THIS addAgent(IAgent agent) {
    this.agents.remove((AGENT) agent);
    this.agents.add((AGENT) agent);
    return (THIS) this;
  }

  public THIS addState(String stateString) {
    this.addState(State.from(stateString));
    return (THIS) this;
  }

  public THIS addState(State state) {
    this.states.remove(state);
    this.states.add(state);
    return (THIS) this;
  }

  public THIS addStates(Collection<State> states) {
    states.forEach(this::addState);
    return (THIS) this;
  }

  public THIS addTransition(State state, Vector<Action> actions, State targetState) {
    var beliefState = Distribution.createSingleEntryDistribution(targetState);
    return addTransition(state, actions, beliefState);
  }

  public THIS addTransition(State state, Vector<Action> actions, Distribution<State> beliefState) {
    this.transitionFunction.putIfAbsent(state, new HashMap<>());
    this.transitionFunction.get(state).put(actions, beliefState);
    return (THIS) this;
  }

  public THIS addReward(State state, Vector<Action> actions, double reward) {
    this.rewardFunction.putIfAbsent(state, new HashMap<>());
    this.rewardFunction.get(state).put(actions, reward);
    return (THIS) this;
  }

  public THIS addObservation(Vector<Action> actions, State targetState, Distribution<Vector<Observation>> observations) {
    this.observationFunction.putIfAbsent(actions, new HashMap<>());
    this.observationFunction.get(actions).put(targetState, observations);
    return (THIS) this;
  }

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