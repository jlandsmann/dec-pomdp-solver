package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

import java.util.List;

public class IsomorphicAgent extends Agent {

  protected final int numberOfAgents;

  protected IsomorphicAgent(String name, List<Action> actions, List<Observation> observations, int numberOfAgents) {
    super(name, actions, observations);
    this.numberOfAgents = numberOfAgents;
  }

  public double getTransitionProbability(State state, Action action, State followState) {
    var transition = getTransition(state, action);
    var probability = transition.getProbability(followState);
    // TODO: check next line
    return Math.pow(probability, numberOfAgents);
  }

  public Distribution<State> getTransition(State state, Action action) {
    // TODO: how to implement this
    return Distribution.createSingleEntryDistribution(state);
  }

  public double getObservationProbability(Action action, State followState, Observation observation) {
    var transition = getObservation(action, followState);
    var probability = transition.getProbability(observation);
    // TODO: check next line
    return Math.pow(probability, numberOfAgents);
  }

  public Distribution<Observation> getObservation(Action action, State nextState) {
    // TODO: how to implement this
    return Distribution.createUniformDistribution(observations);
  }

}
