package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LiftedDecPOMDP extends DecPOMDP<LiftedAgent> {

  protected LiftedDecPOMDP(List<LiftedAgent> agents, List<State> states, double discountFactor, Distribution<State> initialBeliefState) {
    super(agents, states, discountFactor, initialBeliefState);
  }

  public double getTransitionProbability(State state, Vector<Vector<Action>> actions, State newState) {
    var probability = 1D;
    for (int i = 0; i < agents.size(); i++) {
      var agent = agents.get(i);
      var actionVector = actions.get(i);
      probability *= agent.getTransitionProbability(state, actionVector, newState);
    }
    return probability;
  }

  public double getObservationProbability(Vector<Vector<Action>> actions, State newState, Vector<Vector<Observation>> observations) {
    var probability = 1D;
    for (int i = 0; i < agents.size(); i++) {
      var agent = agents.get(i);
      var actionVector = actions.get(i);
      var observationVector = observations.get(i);
      probability *= agent.getObservationProbability(actionVector, newState, observationVector);
    }
    return probability;
  }

  @Override
  public double getReward(State currentState, Vector<Action> agentActions) {
    // TODO: how to calculate the isolated reward of this state?
    return 0;
  }

  public double getReward(State state) {
    // TODO: how to calculate the isolated reward of this state?
    return 0D;
  }
}
