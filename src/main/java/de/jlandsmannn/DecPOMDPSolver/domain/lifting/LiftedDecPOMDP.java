package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IDecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class LiftedDecPOMDP<AGENT extends ILiftedAgent, ACTION extends Histogram<Action>, OBSERVATION extends Histogram<Observation>> extends DecPOMDP<AGENT, ACTION, OBSERVATION> implements IDecPOMDP<AGENT, ACTION, OBSERVATION> {

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

  public int getTotalAgentCount() {
    return getAgents().stream().mapToInt(ILiftedAgent::getNumberOfAgents).sum();
  }

  @Override
  public double getTransitionProbability(State currentState, Vector<ACTION> agentActions, State followState) {
    return Optional
      .ofNullable(transitionFunction.get(currentState))
      .map(t -> t.get(agentActions))
      .map(t -> t.getProbability(followState))
      .orElse(0D);
  }

  public double getReward(State currentState, Vector<ACTION> agentActions) {
    if (agentActions.size() != getAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match agent count.");
    }
    return Optional
      .ofNullable(rewardFunction.get(currentState))
      .map(t -> t.get(agentActions))
      .orElse(0D);
  }

  @Override
  public double getObservationProbability(Vector<ACTION> agentActions, State followState, Vector<OBSERVATION> agentObservations) {
    return Optional
      .ofNullable(observationFunction.get(agentActions))
      .map(t -> t.get(followState))
      .map(t -> t.getProbability(agentObservations))
      .orElse(0D);
  }

  public abstract List<Vector<ACTION>> getActionCombinations();

  public abstract List<Vector<OBSERVATION>> getObservationCombinations();
}
