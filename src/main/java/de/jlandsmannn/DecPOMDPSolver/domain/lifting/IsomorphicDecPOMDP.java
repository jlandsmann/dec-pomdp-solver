
package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.GroundDecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.CombinationCollectors;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorCombinationBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.tuple.Tuple2;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.tuple.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public abstract class IsomorphicDecPOMDP<AGENT extends ILiftedAgent> extends GroundDecPOMDP<AGENT> implements ILiftedDecPOMDP<AGENT> {

  public IsomorphicDecPOMDP(List<AGENT> agents,
                            List<State> states,
                            double discountFactor,
                            Distribution<State> initialBeliefState,
                            Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction,
                            Map<State, Map<Vector<Action>, Double>> rewardFunction,
                            Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction) {
    super(agents, states, discountFactor, initialBeliefState, transitionFunction, rewardFunction, observationFunction);
  }

  public int getTotalAgentCount() {
    return getAgents().stream().mapToInt(ILiftedAgent::getPartitionSize).sum();
  }

  @Override
  public double getTransitionProbability(State currentState, Vector<Action> actionVector, State followState) {
    if (actionVector.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match total agent count.");
    }
    return getGroundings(actionVector)
      .stream()
      .mapToDouble(grounding -> super.getTransitionProbability(currentState, grounding, followState))
      .reduce((a, b) -> a * b)
      .orElse(0D)
    ;
  }

  @Override
  public double getReward(State currentState, Vector<Action> actionVector) {
    if (actionVector.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match total agent count.");
    }
    return getGroundings(actionVector)
      .stream()
      .mapToDouble(grounding -> super.getReward(currentState, grounding))
      .sum();
  }

  @Override
  public double getObservationProbability(Vector<Action> actionVector, State followState, Vector<Observation> observationVector) {
    if (actionVector.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match total agent count.");
    } else if (observationVector.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Length of observation vector doesn't match total agent count.");
    }
    return getGroundings(actionVector, observationVector)
      .stream()
      .mapToDouble(groundings -> super.getObservationProbability(groundings.first(), followState, groundings.second()))
      .reduce((a, b) -> a * b)
      .orElse(0D)
      ;
  }

  @Override
  public List<Vector<Action>> getActionVectors() {
    return getAgents().stream()
      .flatMap(agent -> IntStream.range(0, agent.getPartitionSize()).mapToObj(i -> agent))
      .map(IAgent::getActions)
      .collect(CombinationCollectors.toCombinationVectors())
      .toList();
  }

  @Override
  public List<Vector<Observation>> getObservationVectors() {
    return getAgents().stream()
      .flatMap(agent -> IntStream.range(0, agent.getPartitionSize()).mapToObj(i -> agent))
      .map(IAgent::getObservations)
      .collect(CombinationCollectors.toCombinationVectors())
      .toList();
  }

  protected <U> List<Vector<U>> getGroundings(Vector<U> combination) {
    var combinationAsList = combination.toList();
    var rawCombinations = new ArrayList<List<U>>();
    var offset = 0;
    for (int i = 0; i < getAgents().size(); i++) {
      var agent = getAgents().get(i);
      var numberOfElements = agent.getPartitionSize();
      var elementsForAgent = combinationAsList.subList(offset, offset + numberOfElements);
      offset += numberOfElements;
      rawCombinations.add(elementsForAgent);
    }
    return VectorCombinationBuilder.listOf(rawCombinations);
  }

  protected <U, V> List<Tuple2<Vector<U>, Vector<V>>> getGroundings(Vector<U> combination, Vector<V> combination2) {
    var groundings1 = getGroundings(combination);
    var groundings2 = getGroundings(combination2);

    return IntStream.range(0, groundings1.size())
      .mapToObj(idx -> Tuples.of(groundings1.get(idx), groundings2.get(idx)))
      .toList();
  }
}
