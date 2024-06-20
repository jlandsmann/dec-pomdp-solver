package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.BasicDecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.List;
import java.util.Map;

public abstract class IsomorphicDecPOMDP<AGENT extends IsomorphicAgent> extends BasicDecPOMDP<AGENT> {

  protected IsomorphicDecPOMDP(List<AGENT> agents, List<State> states, double discountFactor, Distribution<State> initialBeliefState,
                               Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction,
                               Map<State, Map<Vector<Action>, Double>> rewardFunction,
                               Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction) {
    super(agents, states, discountFactor, initialBeliefState, transitionFunction, rewardFunction, observationFunction);
  }

  public double getTransitionProbability(State state, Vector<Action> actions, State newState) {
    var probability = 1D;
    for (int i = 0; i < agents.size(); i++) {
      var agent = agents.get(i);
      var actionVector = actions.get(i);
      // TODO: expand action vector to represent all agents explicitly
      probability *= 1;
    }
    return probability;
  }

  public double getObservationProbability(Vector<Action> actions, State newState, Vector<Observation> observations) {
    var probability = 1D;
    for (int i = 0; i < agents.size(); i++) {
      var agent = agents.get(i);
      var action = actions.get(i);
      var observation = observations.get(i);
      // TODO: expand action vector to represent all agents explicitly
      probability *= 1;
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
