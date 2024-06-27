package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.BasicDecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class CountingDecPOMDP<AGENT extends LiftedAgent> extends DecPOMDP<AGENT, Histogram<Action>, Histogram<Observation>> {

  private final Map<State, Map<Vector<Histogram<Action>>, Distribution<State>>> transitionFunction;
  private final Map<State, Map<Vector<Histogram<Action>>, Double>> rewardFunction;
  private final Map<Vector<Histogram<Action>>, Map<State, Distribution<Vector<Histogram<Observation>>>>> observationFunction;

  protected CountingDecPOMDP(List<AGENT> agents, List<State> states, double discountFactor, Distribution<State> initialBeliefState,
                             Map<State, Map<Vector<Histogram<Action>>, Distribution<State>>> transitionFunction,
                             Map<State, Map<Vector<Histogram<Action>>, Double>> rewardFunction,
                             Map<Vector<Histogram<Action>>, Map<State, Distribution<Vector<Histogram<Observation>>>>> observationFunction) {
    super(agents, states, discountFactor, initialBeliefState);
    this.transitionFunction = transitionFunction;
    this.rewardFunction = rewardFunction;
    this.observationFunction = observationFunction;
  }

  @Override
  public int getAgentCount() {
    return agents.stream().mapToInt(LiftedAgent::getNumberOfAgents).sum();
  }

  public Distribution<State> getTransition(State currentState, Vector<Histogram<Action>> agentActions) {
    return transitionFunction.getOrDefault(currentState, Map.of()).get(agentActions);
  }

  public Distribution<Vector<Histogram<Observation>>> getObservations(Vector<Histogram<Action>> agentActions, State nextState) {
    return observationFunction.getOrDefault(agentActions, Map.of()).get(nextState);
  }

  public double getReward(State currentState, Vector<Histogram<Action>> agentActions) {
    return rewardFunction.getOrDefault(currentState, Map.of()).getOrDefault(agentActions, 0D);
  }

  public List<Vector<Histogram<Action>>> getActionCombinations() {
    var actionCombinations = getAgents().stream()
      .map(agent -> HistogramBuilder.listOf(agent.getActions(), agent.getNumberOfAgents()))
      .toList();
    return VectorCombinationBuilder.listOf(actionCombinations);
  }

  public List<Vector<Histogram<Observation>>> getObservationCombinations() {
    var observationCombinations = getAgents().stream()
      .map(agent -> HistogramBuilder.listOf(agent.getObservations(), agent.getNumberOfAgents()))
      .toList();
    return VectorCombinationBuilder.listOf(observationCombinations);
  }
}
