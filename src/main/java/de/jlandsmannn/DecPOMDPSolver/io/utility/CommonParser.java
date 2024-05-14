package de.jlandsmannn.DecPOMDPSolver.io.utility;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorStreamBuilder;
import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;

import java.util.*;

public class CommonParser {

  public static Map<State, Double> parseStatesAndTheirDistributions(List<State> states, String rawStateProbabilities) {
    var stateProbabilities = rawStateProbabilities.split(" ");
    if (stateProbabilities.length > states.size()) {
      throw new IllegalArgumentException("Distribution of states consists of more states than defined.");
    }
    var rawDistribution = new HashMap<State, Double>();
    for (int i = 0; i < stateProbabilities.length; i++) {
      var state = states.get(i);
      var probability = parseDoubleOrThrow(stateProbabilities[i]);
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
      var probability = parseDoubleOrThrow(probabilities[i]);
      rawDistribution.put(vector, probability);
    }
    return rawDistribution;
  }

  public static List<Vector<Action>> parseActionVector(List<List<Action>> agentActions, String rawActionVector) {
    if (rawActionVector.equals("*")) {
      return VectorStreamBuilder.forEachCombination(agentActions).toList();
    }
    var rawActions = rawActionVector.split(" ");
    var listOfActions = new ArrayList<List<Action>>();
    for (int i = 0; i < rawActions.length; i++) {
      var rawAction = rawActions[i];
      var possibleActions = agentActions.get(i);
      var actions = parseActionOrWildcard(possibleActions, rawAction);
      listOfActions.add(i, actions);
    }
    return VectorStreamBuilder.forEachCombination(listOfActions).toList();
  }

  public static List<Vector<Observation>> parseObservationVector(List<List<Observation>> agentObservations, String rawObservationVector) {
    if (rawObservationVector.equals("*")) {
      return VectorStreamBuilder.forEachCombination(agentObservations).toList();
    }
    var rawObservations = rawObservationVector.split(" ");
    var listOfObservations = new ArrayList<List<Observation>>();
    for (int i = 0; i < rawObservations.length; i++) {
      var rawObservation = rawObservations[i];
      var possibleObservations = agentObservations.get(i);
      var observations = parseObservationOrWildcard(possibleObservations, rawObservation);
      listOfObservations.add(i, observations);
    }
    return VectorStreamBuilder.forEachCombination(listOfObservations).toList();
  }

  public static List<State> parseStateOrWildcard(List<State> states, String rawStateString) {
    var state = State.from(rawStateString);
    var index = parseInteger(rawStateString);
    if (rawStateString.equals("*")) {
      return states;
    } else if (index.isPresent()) {
      return List.of(states.get(index.get()));
    } else if (states.contains(state)) {
      return List.of(state);
    } else {
      throw new IllegalArgumentException("state contains unknown state: " + rawStateString);
    }
  }

  public static List<Action> parseActionOrWildcard(List<Action> actions, String rawActionString) {
    var action = Action.from(rawActionString);
    var index = parseInteger(rawActionString);
    if (rawActionString.equals("*")) {
      return actions;
    } else if (index.isPresent()) {
      return List.of(actions.get(index.get()));
    } else if (actions.contains(action)) {
      return List.of(action);
    } else {
      throw new IllegalArgumentException("action contains unknown action: " + rawActionString);
    }
  }

  public static List<Observation> parseObservationOrWildcard(List<Observation> observations, String rawObservationString) {
    var observation = Observation.from(rawObservationString);
    var index = parseInteger(rawObservationString);
    if (rawObservationString.equals("*")) {
      return observations;
    } else if (index.isPresent()) {
      return List.of(observations.get(index.get()));
    } else if (observations.contains(observation)) {
      return List.of(observation);
    } else {
      throw new IllegalArgumentException("observation contains unknown observation: " + rawObservationString);
    }
  }

  public static Optional<Integer> parseInteger(String rawInteger) {
    try {
      var x = Integer.parseInt(rawInteger);
      return Optional.of(x);
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  public static int parseIntegerOrThrow(String rawInteger) {
    return parseInteger(rawInteger).orElseThrow(() -> new ParsingFailedException("Found invalid integer: " + rawInteger));
  }

  public static Optional<Double> parseDouble(String rawDouble) {
    try {
      var x = Double.parseDouble(rawDouble);
      return Optional.of(x);
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  public static double parseDoubleOrThrow(String rawDouble) {
    return parseDouble(rawDouble).orElseThrow(() -> new ParsingFailedException("Found invalid double: " + rawDouble));
  }
}
