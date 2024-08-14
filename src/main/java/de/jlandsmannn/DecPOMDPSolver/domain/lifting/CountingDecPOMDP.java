
package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class CountingDecPOMDP<AGENT extends ILiftedAgent> extends DecPOMDP<AGENT> implements ILiftedDecPOMDP<AGENT> {

  private final Map<State, Map<Histogram<Action>, Distribution<State>>> transitionFunction;
  private final Map<State, Map<Histogram<Action>, Double>> rewardFunction;
  private final Map<Histogram<Action>, Map<State, Distribution<Histogram<Observation>>>> observationFunction;

  public CountingDecPOMDP(List<AGENT> agents,
                          List<State> states,
                          double discountFactor,
                          Distribution<State> initialBeliefState,
                          Map<State, Map<Histogram<Action>, Distribution<State>>> transitionFunction,
                          Map<State, Map<Histogram<Action>, Double>> rewardFunction,
                          Map<Histogram<Action>, Map<State, Distribution<Histogram<Observation>>>> observationFunction) {
    super(agents, states, discountFactor, initialBeliefState);
    this.transitionFunction = transitionFunction;
    this.rewardFunction = rewardFunction;
    this.observationFunction = observationFunction;
  }

  public int getTotalAgentCount() {
    return getAgents().stream().mapToInt(ILiftedAgent::getPartitionSize).sum();
  }

  @Override
  public double getTransitionProbability(State currentState, Vector<Action> agentActions, State followState) {
    if (agentActions.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match total agent count.");
    }
    var actionHistogram = Histogram.from(agentActions);
    return Optional
      .ofNullable(transitionFunction.get(currentState))
      .map(t -> t.get(actionHistogram))
      .map(t -> t.getProbability(followState))
      .orElse(0D);
  }

  @Override
  public double getReward(State currentState, Vector<Action> agentActions) {
    if (agentActions.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match total agent count.");
    }
    var actionHistogram = Histogram.from(agentActions);
    return Optional
      .ofNullable(rewardFunction.get(currentState))
      .map(t -> t.get(actionHistogram))
      .orElse(0D);
  }

  @Override
  public double getObservationProbability(Vector<Action> agentActions, State followState, Vector<Observation> agentObservations) {
    if (agentActions.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match total agent count.");
    } else if (agentObservations.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Length of observation vector doesn't match total agent count.");
    }

    var actionHistogram = Histogram.from(agentActions);
    var observationHistogram = Histogram.from(agentObservations);
    return Optional
      .ofNullable(observationFunction.get(actionHistogram))
      .map(t -> t.get(followState))
      .map(t -> t.getProbability(observationHistogram))
      .orElse(0D);
  }

  @Override
  public List<Vector<Action>> getActionCombinations() {
    return getAgents().stream()
      .map(agent -> HistogramBuilder.listOf(agent.getActions(), agent.getPartitionSize()))
      .collect(CombinationCollectors.toCombinationVectors())
      .map(vector -> vector.stream()
        .flatMap(histogram -> histogram.toList().stream())
        .collect(CustomCollectors.toVector())
      )
      .toList();
  }

  @Override
  public List<Vector<Observation>> getObservationCombinations() {
    return getAgents().stream()
      .map(agent -> HistogramBuilder.listOf(agent.getObservations(), agent.getPartitionSize()))
      .collect(CombinationCollectors.toCombinationVectors())
      .map(vector -> vector.stream()
        .flatMap(histogram -> histogram.toList().stream())
        .collect(CustomCollectors.toVector())
      )
      .toList();
  }
}
