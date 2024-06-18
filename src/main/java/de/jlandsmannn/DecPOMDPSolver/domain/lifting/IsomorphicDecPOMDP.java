package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.List;

public abstract class IsomorphicDecPOMDP extends DecPOMDP<IsomorphicAgent> {

  protected IsomorphicDecPOMDP(List<IsomorphicAgent> agents, List<State> states, double discountFactor, Distribution<State> initialBeliefState) {
    super(agents, states, discountFactor, initialBeliefState);
  }

  public double getTransitionProbability(State state, Vector<Action> actions, State newState) {
    var probability = 1D;
    for (int i = 0; i < agents.size(); i++) {
      var agent = agents.get(i);
      var actionVector = actions.get(i);
      probability *= agent.getTransitionProbability(state, actionVector, newState);
    }
    return probability;
  }

  public double getObservationProbability(Vector<Action> actions, State newState, Vector<Observation> observations) {
    var probability = 1D;
    for (int i = 0; i < agents.size(); i++) {
      var agent = agents.get(i);
      var action = actions.get(i);
      var observation = observations.get(i);
      probability *= agent.getObservationProbability(action, newState, observation);
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
