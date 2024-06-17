package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import com.google.common.collect.Streams;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.utils.ActionObservationPair;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.List;
import java.util.stream.Stream;

public class LiftedAgent extends Agent {

  protected final int numberOfAgents;

  protected LiftedAgent(String name, List<Action> actions, List<Observation> observations, int numberOfAgents) {
    super(name, actions, observations);
    this.numberOfAgents = numberOfAgents;
  }

  public double getTransitionProbability(State state, Vector<Action> actionVector, State followState) {
    return actionVector.stream()
      .mapToDouble(action -> getTransitionProbability(state, action, followState))
      .reduce((a,b) -> a * b)
      .orElse(0D);
  }

  public double getTransitionProbability(State state, Action action, State followState) {
    return getTransition(state, action).getProbability(followState);
  }

  public Distribution<State> getTransition(State state, Action action) {
    // TODO: how to implement this
    return Distribution.createSingleEntryDistribution(state);
  }

  public double getObservationProbability(Vector<Action> actionVector, State followState, Vector<Observation> observationVector) {
    return Streams.zip(actionVector.stream(), observationVector.stream(), ActionObservationPair::new)
      .mapToDouble((pair) -> getObservationProbability(pair.action(), followState, pair.observation()))
      .reduce((a,b) -> a * b)
      .orElse(0D);
  }

  public double getObservationProbability(Action action, State followState, Observation observation) {
    return this.getObservation(action, followState).getProbability(observation);
  }

  public Distribution<Observation> getObservation(Action action, State nextState) {
    // TODO: how to implement this
    return Distribution.createUniformDistribution(observations);
  }

}
