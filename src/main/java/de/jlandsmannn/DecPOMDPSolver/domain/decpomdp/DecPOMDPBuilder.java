package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DecPOMDPBuilder {
  private final Logger logger = LoggerFactory.getLogger(DecPOMDPBuilder.class);
  private final List<AgentWithStateController> agents = new ArrayList<>();
  private final List<State> states = new ArrayList<>();
  private final Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction = new HashMap<>();
  private final Map<State, Map<Vector<Action>, Double>> rewardFunction = new HashMap<>();
  private final Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction = new HashMap<>();
  private double discountFactor;
  private Distribution<State> initialBeliefState;

  public DecPOMDPBuilder addAgents(Collection<AgentWithStateController> agents) {
    this.agents.removeAll(agents);
    this.agents.addAll(agents);
    return this;
  }

  public DecPOMDPBuilder addAgent(AgentWithStateController agent) {
    this.agents.remove(agent);
    this.agents.add(agent);
    return this;
  }

  public DecPOMDPBuilder addState(String stateString) {
    this.addState(State.from(stateString));
    return this;
  }

  public DecPOMDPBuilder addState(State state) {
    this.states.remove(state);
    this.states.add(state);
    return this;
  }

  public DecPOMDPBuilder addStates(Collection<State> states) {
    states.forEach(this::addState);
    return this;
  }

  public DecPOMDPBuilder addTransition(String stateString, Vector<Action> actions, String targetState) {
    var beliefState = Distribution.createSingleEntryDistribution(new State(targetState));
    return addTransition(stateString, actions, beliefState);
  }

  public DecPOMDPBuilder addTransition(String stateString, Vector<Action> actions, Distribution<State> beliefState) {
    var state = new State(stateString);
    return addTransition(state, actions, beliefState);
  }

  public DecPOMDPBuilder addTransition(State state, Vector<Action> actions, Distribution<State> beliefState) {
    this.transitionFunction.putIfAbsent(state, new HashMap<>());
    this.transitionFunction.get(state).put(actions, beliefState);
    return this;
  }

  public DecPOMDPBuilder addReward(String stateString, Vector<Action> actions, Double reward) {
    var state = new State(stateString);
    return addReward(state, actions, reward);
  }

  public DecPOMDPBuilder addReward(State state, Vector<Action> actions, Double reward) {
    this.rewardFunction.putIfAbsent(state, new HashMap<>());
    this.rewardFunction.get(state).put(actions, reward);
    return this;
  }

  public DecPOMDPBuilder addObservation(Vector<Action> actions, String targetStateString, Distribution<Vector<Observation>> observations) {
    State targetState = new State(targetStateString);
    return addObservation(actions, targetState, observations);
  }

  public DecPOMDPBuilder addObservation(Vector<Action> actions, State targetState, Distribution<Vector<Observation>> observations) {
    this.observationFunction.putIfAbsent(actions, new HashMap<>());
    this.observationFunction.get(actions).put(targetState, observations);
    return this;
  }

  public DecPOMDPBuilder setDiscountFactor(double discountFactor) {
    this.discountFactor = discountFactor;
    return this;
  }

  public DecPOMDPBuilder setInitialBeliefState(Distribution<State> initialBeliefState) {
    this.initialBeliefState = initialBeliefState;
    return this;
  }

  public DecPOMDPWithStateController createDecPOMDP() {
    logger.info("Creating CommonDecPOMDP with {} agents, {} states, {} transitions, {} rewards and {} observations.",
      agents.size(), states.size(), transitionFunction.size(), rewardFunction.size(), observationFunction.size());
    return new DecPOMDPWithStateController(agents, states, discountFactor, initialBeliefState, transitionFunction, rewardFunction, observationFunction);
  }

  public List<State> getStates() {
    return states;
  }

  public double getDiscountFactor() {
    return discountFactor;
  }
}