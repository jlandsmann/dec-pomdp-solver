package de.jlandsmannn.DecPOMDPSolver.io.utility;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorStreamBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonParser {

  public static Map<State, Double> parseStatesAndTheirDistributions(List<State> states, String rawStateProbabilities) {
    var stateProbabilities = rawStateProbabilities.split(" ");
    if (stateProbabilities.length > states.size()) {
      throw new IllegalArgumentException("Distribution of states consists of more states than defined.");
    }
    var rawDistribution = new HashMap<State, Double>();
    for (int i = 0; i < stateProbabilities.length; i++) {
      var state = states.get(i);
      var probability = Double.parseDouble(stateProbabilities[i]);
      rawDistribution.put(state, probability);
    }
    return rawDistribution;
  }

  public static Map<Vector<Observation>, Double> parseObservationVectorsAndTheirDistributions(List<List<Observation>> agentObservations, String rawProbabilities) {
    var observationCombinations = VectorStreamBuilder.forEachCombination(agentObservations).toList();
    var probabilities = rawProbabilities.split(" ");
    if (probabilities.length > observationCombinations.size()) {
      throw new IllegalArgumentException("Distribution of observation vectors consists of more vectors than defined.");
    }
    var rawDistribution = new HashMap<Vector<Observation>, Double>();
    for (int i = 0; i < probabilities.length; i++) {
      var vector = observationCombinations.get(i);
      var probability = Double.parseDouble(probabilities[i]);
      rawDistribution.put(vector, probability);
    }
    return rawDistribution;
  }

  public static List<Vector<Action>> parseActionVector(List<List<Action>> agentActions, String rawActionVector) {
    if (rawActionVector.equals("*")) {
      return VectorStreamBuilder.forEachCombination(agentActions).toList();
    }
    var actions = rawActionVector.split(" ");
    var listOfActions = new ArrayList<List<Action>>();
    for (int i = 0; i < actions.length; i++) {
      var actionName = actions[i];
      var possibleActions = agentActions.get(i);
      if (actionName.equals("*")) {
        listOfActions.add(i, possibleActions);
      } else if (possibleActions.contains(Action.from(actionName))) {
        listOfActions.add(i, Action.listOf(actionName));
      };
    }
    return VectorStreamBuilder.forEachCombination(listOfActions).toList();
  }

  public static List<Vector<Observation>> parseObservationVector(List<List<Observation>> agentObservations, String rawObservationVector) {
    if (rawObservationVector.equals("*")) {
      return VectorStreamBuilder.forEachCombination(agentObservations).toList();
    }
    var observations = rawObservationVector.split(" ");
    var listOfObservations = new ArrayList<List<Observation>>();
    for (int i = 0; i < observations.length; i++) {
      var observationName = observations[i];
      var possibleObservations = agentObservations.get(i);
      if (observationName.equals("*")) {
        listOfObservations.add(i, possibleObservations);
      } else if (possibleObservations.contains(Observation.from(observationName))) {
        listOfObservations.add(i, Observation.listOf(observationName));
      };
    }
    return VectorStreamBuilder.forEachCombination(listOfObservations).toList();
  }

  public static List<State> parseStateOrWildcard(List<State> states, String rawStateString) {
    var state = State.from(rawStateString);
    if (rawStateString.equals("*")) {
      return states;
    } else if (states.contains(state)) {
      return List.of(state);
    } else {
      throw new IllegalArgumentException("state contains unknown state.");
    }
  }
}
