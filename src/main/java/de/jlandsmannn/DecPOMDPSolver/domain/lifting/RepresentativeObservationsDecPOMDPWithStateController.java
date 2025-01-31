package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RepresentativeObservationsDecPOMDPWithStateController extends IsomorphicDecPOMDPWithStateController {

  private final long groundingConstant;

  protected RepresentativeObservationsDecPOMDPWithStateController(List<IsomorphicAgentWithStateController> agents,
                                                                  List<State> states,
                                                                  double discountFactor,
                                                                  Distribution<State> initialBeliefState,
                                                                  Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction,
                                                                  Map<State, Map<Vector<Action>, Double>> rewardFunction,
                                                                  Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction) {
    super(agents, states, discountFactor, initialBeliefState, transitionFunction, rewardFunction, observationFunction);
    groundingConstant = agents.stream().mapToLong(ILiftedAgent::getPartitionSize).reduce(Math::multiplyExact).orElseThrow();
  }

  @Override
  public double getTransitionProbability(State currentState, Vector<Action> agentActions, State followState) {
    if (agentActions.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match total agent count.");
    }
    var actionVector = getAnyGrounding(agentActions);
    var probability = doGetTransitionProbability(currentState, actionVector, followState);
    return Math.pow(probability, groundingConstant);
  }

  @Override
  public double getReward(State currentState, Vector<Action> agentActions) {
    if (agentActions.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match total agent count.");
    }
    var actionVector = getAnyGrounding(agentActions);
    var reward = doGetReward(currentState, actionVector);
    return reward * groundingConstant;
  }

  @Override
  public double getObservationProbability(Vector<Action> agentActions, State followState, Vector<Observation> agentObservations) {
    if (agentActions.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Length of action vector doesn't match total agent count.");
    } else if (agentObservations.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Length of observation vector doesn't match total agent count.");
    }
    var actionVector = getAnyGrounding(agentActions);
    var observationVector = getAnyGrounding(agentObservations);
    var probability = doGetObservationProbability(actionVector, followState, observationVector);
    return Math.pow(probability, groundingConstant);
  }

  @Override
  public double getActionVectorProbability(Vector<Node> nodes, Vector<Action> actions) {
    if (nodes.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Number of nodes does not match total number of agents");
    } else if (actions.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Number of actions does not match total number of agents");
    }
    var probability = 1D;
    var nodeVector = getAnyGrounding(nodes);
    var actionVector = getAnyGrounding(actions);
    for (int i = 0; i < getAgents().size(); i++) {
      var agent = getAgents().get(i);
      var node = nodeVector.get(i);
      var action = actionVector.get(i);
      var selectionProbability = agent.getActionSelectionProbability(node, action);
      probability *= Math.pow(selectionProbability, agent.getPartitionSize());
    }
    return probability;
  }

  @Override
  public double getNodeTransitionProbability(Vector<Node> nodes, Vector<Action> actions, Vector<Observation> observations, Vector<Node> newNodes) {
    if (nodes.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Number of nodes does not match total number of agents");
    } else if (actions.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Number of actions does not match total number of agents");
    } else if (observations.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Number of observations does not match total number of agents");
    } else if (newNodes.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Number of newNodes does not match total number of agents");
    }
    var probability = 1D;
    var nodeVector = getAnyGrounding(nodes);
    var actionVector = getAnyGrounding(actions);
    var observationVector = getAnyGrounding(observations);
    var newNodeVector = getAnyGrounding(newNodes);
    for (int i = 0; i < getAgents().size(); i++) {
      var agent = getAgents().get(i);
      var node = nodeVector.get(i);
      var action = actionVector.get(i);
      var observation = observationVector.get(i);
      var newNode = newNodeVector.get(i);
      var transitionProbability = agent.getNodeTransitionProbability(node, action, observation, newNode);
      probability *= Math.pow(transitionProbability, agent.getPartitionSize());
    }
    return probability;
  }

  @Override
  public List<Vector<Node>> getNodeCombinations() {
    return getAgents().stream()
      .map(agent -> HistogramBuilder.listOfPeakShaped(agent.getControllerNodes(), agent.getPartitionSize()))
      .collect(CombinationCollectors.toCombinationVectors())
      .map(vector -> vector.stream()
        .flatMap(histogram -> histogram.toList().stream())
        .collect(CustomCollectors.toVector())
      )
      .toList();
  }

  @Override
  public List<Vector<Node>> getNodeCombinations(Vector<Node> nodeVector) {
    var rawNodeCombinations = new ArrayList<List<Histogram<Node>>>();
    var offset = 0;
    for (int i = 0; i < getAgents().size(); i++) {
      var agent = getAgents().get(i);
      var node = nodeVector.get(offset);
      var followNodes = agent.getFollowNodes(node);
      var histograms = HistogramBuilder.listOfPeakShaped(followNodes, agent.getPartitionSize());
      rawNodeCombinations.add(histograms);
      offset += agent.getPartitionSize();
    }
    return rawNodeCombinations
      .stream()
      .collect(CombinationCollectors.toCombinationVectors())
      .map(vector -> vector.stream()
        .flatMap(histogram -> histogram.toList().stream())
        .collect(CustomCollectors.toVector())
      )
      .toList();
  }

  @Override
  public List<Vector<Action>> getActionVectors() {
    return getAgents().stream()
      .map(agent -> HistogramBuilder.listOfPeakShaped(agent.getActions(), agent.getPartitionSize()))
      .collect(CombinationCollectors.toCombinationVectors())
      .map(vector -> vector.stream()
        .flatMap(histogram -> histogram.toList().stream())
        .collect(CustomCollectors.toVector())
      )
      .toList();
  }

  @Override
  public List<Vector<Action>> getActionCombinations(Vector<Node> nodeVector) {
    List<List<Histogram<Action>>> rawActionCombinations = new ArrayList<>();
    var offset = 0;
    for (int i = 0; i < getAgents().size(); i++) {
      var agent = getAgents().get(i);
      var node = nodeVector.get(offset);
      var actions = agent.getSelectableActions(node);
      var histograms = HistogramBuilder.listOfPeakShaped(actions, agent.getPartitionSize());
      rawActionCombinations.add(histograms);
      offset += agent.getPartitionSize();
    }
    return rawActionCombinations
      .stream()
      .collect(CombinationCollectors.toCombinationVectors())
      .map(vector -> vector.stream()
        .flatMap(histogram -> histogram.toList().stream())
        .collect(CustomCollectors.toVector())
      )
      .toList();
  }

  @Override
  public List<Vector<Observation>> getObservationVectors() {
    return getAgents().stream()
      .map(agent -> HistogramBuilder.listOfPeakShaped(agent.getObservations(), agent.getPartitionSize()))
      .collect(CombinationCollectors.toCombinationVectors())
      .map(vector -> vector.stream()
        .flatMap(histogram -> histogram.toList().stream())
        .collect(CustomCollectors.toVector())
      )
      .toList();
  }

  public <U> Vector<U> getAnyGrounding(Vector<U> input) {
    if (input.size() != getTotalAgentCount()) {
      throw new IllegalArgumentException("Number of elements in input vector does not match total number of agents");
    }
    var output = new ArrayList<U>();
    var offset = 0;
    for (int i = 0; i < getAgents().size(); i++) {
      var agent = getAgents().get(i);
      var element = input.get(offset);
      output.add(element);
      offset += agent.getPartitionSize();
    }
    return Vector.of(output);
  }
}
