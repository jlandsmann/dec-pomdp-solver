package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorCombinationBuilder;
import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern.*;
import static de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPCommonKeyword.ANY;
import static de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPCommonKeyword.UNIFORM;

/**
 * This parser covers the "O" section of the .dpomdp file format.
 * That section describes the observation's probabilities
 * with respect to the selected action vector and the current state.
 * To validate the given observation function,
 * it needs the states as well as the agent's actions and observations.
 * Since later entries can overwrite earlier ones,
 * it also needs the previously collected observation entries.
 */
public class ObservationEntrySectionParser extends BaseSectionParser {
  private static final Logger LOG = LoggerFactory.getLogger(ObservationEntrySectionParser.class);

  protected List<State> states = new ArrayList<>();
  protected List<List<Action>> agentActions = new ArrayList<>();
  protected List<List<Observation>> agentObservations = new ArrayList<>();
  protected Map<Vector<Action>, Map<State, Map<Vector<Observation>, Double>>> observations = new HashMap<>();

  public ObservationEntrySectionParser() {
    super(
      DPOMDPSectionKeyword.OBSERVATION_ENTRY,
      DPOMDPSectionKeyword.OBSERVATION_ENTRY + ": ?" +
        NAMED_GROUP("actionVector", LIST_OF(OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern()))) +
        " ?: ?" +
        OR(
          NAMED_GROUP("endState", OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern())) +
            " ?: ?" +
            OR(
              NAMED_GROUP("observationVector", LIST_OF(OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern()))) +
                " ?: ?" + NAMED_GROUP("probability", PROBABILITY_PATTERN),
              "\n" + NAMED_GROUP("probabilityDistribution", LIST_OF(PROBABILITY_PATTERN))
            ),
          // uniform probability distribution
          "\n" + NAMED_GROUP("probabilityUniformDistribution", UNIFORM.getPattern()),
          // matrix defined for given action vector
          "\n" + NAMED_GROUP("probabilityMatrix", ROWS_OF(LIST_OF(PROBABILITY_PATTERN)))
        )
    );
  }

  public Map<Vector<Action>, Map<State, Map<Vector<Observation>, Double>>> getObservations() {
    return observations;
  }

  public ObservationEntrySectionParser setStates(List<State> states) {
    this.states = states;
    return this;
  }

  public ObservationEntrySectionParser setAgentActions(List<List<Action>> agentActions) {
    this.agentActions = agentActions;
    return this;
  }

  public ObservationEntrySectionParser setAgentObservations(List<List<Observation>> agentObservations) {
    this.agentObservations = agentObservations;
    return this;
  }

  public ObservationEntrySectionParser setObservations(Map<Vector<Action>, Map<State, Map<Vector<Observation>, Double>>> observations) {
    this.observations = observations;
    return this;
  }

  public void parseSection(String section) {
    LOG.debug("Parsing 'O' section.");
    assertAllDependenciesSet();
    var match = getMatchOrThrow(section);
    if (!match.hasGroup("actionVector")) throw new ParsingFailedException("'O' section was parsed successfully, but actionVector is not present.");
    var actionVectors = CommonParser.parseActionVector(agentActions, match.getGroupAsStringOrThrow("actionVector"));
    if (match.hasGroup("endState")) {
      var endStates = CommonParser.parseStateOrWildcard(states, match.getGroupAsStringOrThrow("endState"));
      if (match.hasGroup("observationVector") && match.hasGroup("probability")) {
        var observationVectors = CommonParser.parseObservationVector(agentObservations, match.getGroupAsStringOrThrow("observationVector"));
        var probability = match.getGroupAsDoubleOrThrow("probability");
        actionVectors.forEach(actionVector -> {
          endStates.forEach(startState -> {
            observationVectors.forEach(observationVector -> {
              saveObservationRule(actionVector, startState, observationVector, probability);
            });
          });
        });
      } else if (match.hasGroup("probabilityDistribution")) {
        var probabilities = CommonParser.parseObservationVectorsAndTheirDistributions(agentObservations, match.getGroupAsStringOrThrow("probabilityDistribution"));
        actionVectors.forEach(actionVector -> {
          endStates.forEach(endState -> {
            saveObservationRule(actionVector, endState, probabilities);
          });
        });
      }
    } else if (match.hasGroup("probabilityUniformDistribution")) {
      var endStates = states;
      var observationCombinations = VectorCombinationBuilder.listOf(agentObservations);
      var distribution = Distribution.createUniformDistribution(observationCombinations);
      var probabilities = distribution.toMap();
      actionVectors.forEach(actionVector -> {
        endStates.forEach(endState -> {
          saveObservationRule(actionVector, endState, probabilities);
        });
      });
    } else if (match.hasGroup("probabilityMatrix")) {
      var rawProbabilityDistributionRows = match.getGroupAsStringArrayOrThrow("probabilityMatrix", "\n");
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

  private void assertAllDependenciesSet() {
    if (states.isEmpty()) {
      throw new ParsingFailedException("'O' section was parsed, before 'states' have been initialized.");
    } else if (agentActions.isEmpty()) {
      throw new ParsingFailedException("'O' section was parsed, before 'actions' have been initialized.");
    } else if (agentObservations.isEmpty()) {
      throw new ParsingFailedException("'O' section was parsed, before 'observations' have been initialized.");
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
