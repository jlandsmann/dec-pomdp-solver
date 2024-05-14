package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorStreamBuilder;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObservationEntryParser {
  private static final Logger LOG = LoggerFactory.getLogger(ObservationEntryParser.class);

  protected List<State> states = new ArrayList<>();
  protected List<List<Action>> agentActions = new ArrayList<>();
  protected List<List<Observation>> agentObservations = new ArrayList<>();
  protected Map<Vector<Action>, Map<State, Map<Vector<Observation>, Double>>> observations = new HashMap<>();

  public Map<Vector<Action>, Map<State, Map<Vector<Observation>, Double>>> getObservations() {
    return observations;
  }

  public ObservationEntryParser setStates(List<State> states) {
    this.states = states;
    return this;
  }

  public ObservationEntryParser setAgentActions(List<List<Action>> agentActions) {
    this.agentActions = agentActions;
    return this;
  }

  public ObservationEntryParser setAgentObservations(List<List<Observation>> agentObservations) {
    this.agentObservations = agentObservations;
    return this;
  }

  public ObservationEntryParser setObservations(Map<Vector<Action>, Map<State, Map<Vector<Observation>, Double>>> observations) {
    this.observations = observations;
    return this;
  }

  public void parseObservationEntry(String section) {
    LOG.debug("Parsing 'O' section.");
    if (states.isEmpty()) {
      throw new IllegalStateException("'O' section was parsed, before 'states' have been initialized.");
    } else if (agentActions.isEmpty()) {
      throw new IllegalStateException("'O' section was parsed, before 'actions' have been initialized.");
    } else if (agentObservations.isEmpty()) {
      throw new IllegalStateException("'O' section was parsed, before 'observations' have been initialized.");
    }
    var match = DPOMDPSectionPattern.OBSERVATION_ENTRY
      .getMatch(section)
      .orElseThrow(() -> new IllegalArgumentException("Trying to parse 'O' section, but found invalid format."));
    if (match.group("actionVector") == null) throw new IllegalStateException("'O' section was parsed successfully, but actionVector is not present.");
    var actionVectors = CommonParser.parseActionVector(agentActions, match.group("actionVector"));
    if (match.group("endState") != null) {
      var endStates = CommonParser.parseStateOrWildcard(states, match.group("endState"));
      if (match.group("observationVector") != null && match.group("probability") != null) {
        var observationVectors = CommonParser.parseObservationVector(agentObservations, match.group("observationVector"));
        var probability = Double.parseDouble(match.group("probability"));
        actionVectors.forEach(actionVector -> {
          endStates.forEach(startState -> {
            observationVectors.forEach(observationVector -> {
              saveObservationRule(actionVector, startState, observationVector, probability);
            });
          });
        });
      } else if (match.group("probabilityDistribution") != null) {
        var probabilities = CommonParser.parseObservationVectorsAndTheirDistributions(agentObservations, match.group("probabilityDistribution"));
        actionVectors.forEach(actionVector -> {
          endStates.forEach(endState -> {
            saveObservationRule(actionVector, endState, probabilities);
          });
        });
      }
    } else if (match.group("probabilityUniformDistribution") != null) {
      var endStates = states;
      var observationCombinations = VectorStreamBuilder.forEachCombination(agentObservations).toList();
      var distribution = Distribution.createUniformDistribution(observationCombinations);
      var probabilities = distribution.toMap();
      actionVectors.forEach(actionVector -> {
        endStates.forEach(endState -> {
          saveObservationRule(actionVector, endState, probabilities);
        });
      });
    } else if (match.group("probabilityMatrix") != null) {
      var rawProbabilityDistributionRows = match.group("probabilityMatrix").split("\n");

      for (int i = 0; i < rawProbabilityDistributionRows.length; i++) {
        var endState = states.get(i);
        var rawProbabilityDistribution = rawProbabilityDistributionRows[i];
        var probabilities = CommonParser.parseObservationVectorsAndTheirDistributions(agentObservations, rawProbabilityDistribution);

        actionVectors.forEach(actionVector -> {
          saveObservationRule(actionVector, endState, probabilities);
        });
      }
    }
  }

  private void saveObservationRule(Vector<Action> actionVector, State endState, Vector<Observation> observationVector, double probability) {
    observations.putIfAbsent(actionVector, new HashMap<>());
    observations.get(actionVector).putIfAbsent(endState, new HashMap<>());
    observations.get(actionVector).get(endState).put(observationVector, probability);
  }

  private void saveObservationRule(Vector<Action> actionVector, State endState, Map<Vector<Observation>, Double> observationProbabilities) {
    observations.putIfAbsent(actionVector, new HashMap<>());
    observations.get(actionVector).putIfAbsent(endState, new HashMap<>());
    observations.get(actionVector).get(endState).putAll(observationProbabilities);
  }
}
