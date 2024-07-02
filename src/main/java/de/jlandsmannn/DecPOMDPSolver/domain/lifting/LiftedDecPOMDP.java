package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.*;

import java.util.List;
import java.util.Map;

public abstract class LiftedDecPOMDP<AGENT extends ILiftedAgent, ACTION extends Histogram<Action>, OBSERVATION extends Histogram<Observation>> extends DecPOMDP<AGENT, ACTION, OBSERVATION> {

  private final Map<State, Map<Vector<ACTION>, Distribution<State>>> transitionFunction;
  private final Map<State, Map<Vector<ACTION>, Double>> rewardFunction;
  private final Map<Vector<ACTION>, Map<State, Distribution<Vector<OBSERVATION>>>> observationFunction;

  protected LiftedDecPOMDP(List<AGENT> agents, List<State> states, double discountFactor, Distribution<State> initialBeliefState,
                           Map<State, Map<Vector<ACTION>, Distribution<State>>> transitionFunction,
                           Map<State, Map<Vector<ACTION>, Double>> rewardFunction,
                           Map<Vector<ACTION>, Map<State, Distribution<Vector<OBSERVATION>>>> observationFunction) {
    super(agents, states, discountFactor, initialBeliefState);
    this.transitionFunction = transitionFunction;
    this.rewardFunction = rewardFunction;
    this.observationFunction = observationFunction;
  }

  @Override
  public int getAgentCount() {
    return agents.stream().mapToInt(ILiftedAgent::getNumberOfAgents).sum();
  }

  public Distribution<State> getTransition(State currentState, Vector<ACTION> agentActions) {
    return transitionFunction.getOrDefault(currentState, Map.of()).get(agentActions);
  }

  public Distribution<Vector<OBSERVATION>> getObservations(Vector<ACTION> agentActions, State nextState) {
    return observationFunction.getOrDefault(agentActions, Map.of()).get(nextState);
  }

  public double getReward(State currentState, Vector<ACTION> agentActions) {
    return rewardFunction.getOrDefault(currentState, Map.of()).getOrDefault(agentActions, 0D);
  }

  public abstract List<Vector<ACTION>> getActionCombinations();

  public abstract List<Vector<OBSERVATION>> getObservationCombinations();
}
