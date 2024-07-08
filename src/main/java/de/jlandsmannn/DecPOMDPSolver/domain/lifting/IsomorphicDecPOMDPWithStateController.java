package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.*;

import java.util.List;
import java.util.Map;

public class IsomorphicDecPOMDPWithStateController extends LiftedDecPOMDPWithStateController<Histogram<Action>, Histogram<Observation>, Histogram<Node>> {

  protected IsomorphicDecPOMDPWithStateController(List<LiftedAgentWithStateController> countingAgentWithStateControllers,
                                                  List<State> states,
                                                  double discountFactor,
                                                  Distribution<State> initialBeliefState,
                                                  Map<State, Map<Vector<Histogram<Action>>, Distribution<State>>> transitionFunction,
                                                  Map<State, Map<Vector<Histogram<Action>>, Double>> rewardFunction,
                                                  Map<Vector<Histogram<Action>>, Map<State, Distribution<Vector<Histogram<Observation>>>>> observationFunction) {
    super(countingAgentWithStateControllers, states, discountFactor, initialBeliefState, transitionFunction, rewardFunction, observationFunction);
  }

  @Override
  public double getActionVectorProbability(Vector<Histogram<Node>> nodes, Vector<Histogram<Action>> actions) {
    var probability = 1D;
    for (int i = 0; i < agents.size(); i++) {
      var agent = agents.get(i);
      var nodeHistogram = nodes.get(i);
      var actionHistogram = actions.get(i);
      probability *= agent.getActionSelectionProbability(nodeHistogram, actionHistogram);
    }
    return probability;
  }

  @Override
  public double getNodeTransitionProbability(Vector<Histogram<Node>> nodes, Vector<Histogram<Action>> actions, Vector<Histogram<Observation>> observations, Vector<Histogram<Node>> newNodes) {
    var probability = 1D;
    for (int i = 0; i < agents.size(); i++) {
      var agent = agents.get(i);
      var nodeHistogram = nodes.get(i);
      var actionHistogram = actions.get(i);
      var observationHistogram = observations.get(i);
      var newNodeHistogram = newNodes.get(i);
      probability *= agent.getNodeTransitionProbability(nodeHistogram, actionHistogram, observationHistogram, newNodeHistogram);
    }
    return probability;
  }

  private <U> List<Vector<U>> getGroundings(Vector<Histogram<U>> combination) {
    var rawCombinations = combination.stream().map(Histogram::toList).toList();
    return VectorCombinationBuilder.listOf(rawCombinations);
  }

  @Override
  public List<Vector<Histogram<Node>>> getNodeCombinations() {
    var combinations = getAgents().stream()
      .map(agent -> HistogramBuilder.listOf(agent.getControllerNodes(), agent.getNumberOfAgents()))
      .toList();
    return VectorCombinationBuilder.listOf(combinations);
  }

  @Override
  public List<Vector<Histogram<Action>>> getActionCombinations() {
    var combinations = getAgents().stream()
      .map(agent -> HistogramBuilder.listOf(agent.getActions(), agent.getNumberOfAgents()))
      .toList();
    return VectorCombinationBuilder.listOf(combinations);
  }

  @Override
  public List<Vector<Histogram<Observation>>> getObservationCombinations() {
    var combinations = getAgents().stream()
      .map(agent -> HistogramBuilder.listOf(agent.getObservations(), agent.getNumberOfAgents()))
      .toList();
    return VectorCombinationBuilder.listOf(combinations);
  }
}
