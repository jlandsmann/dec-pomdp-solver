package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
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
import static de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPCommonKeyword.*;

/**
 * This parser covers the "T" section of the .dpomdp file format.
 * That section describes the transition function's probabilities
 * with respect to the selected action vector and the current state
 * as well as the following state.
 * The distribution of the following states can be defined,
 * either by uniform or identity distribution,
 * or by a start and follow state matrix probability.
 * To validate the given transition function,
 * it needs the states as well as the agent's actions.
 * Since later entries can overwrite earlier ones,
 * it also needs the previously collected transition entries.
 */
public class TransitionEntrySectionParser extends BaseSectionParser {
  private static final Logger LOG = LoggerFactory.getLogger(TransitionEntrySectionParser.class);

  protected List<State> states = new ArrayList<>();
  protected List<List<Action>> agentActions = new ArrayList<>();
  protected Map<State, Map<Vector<Action>, Map<State, Double>>> transitions = new HashMap<>();

  public TransitionEntrySectionParser() {
    super(
      DPOMDPSectionKeyword.TRANSITION_ENTRY,
      DPOMDPSectionKeyword.TRANSITION_ENTRY + ": ?" +
        NAMED_GROUP("actionVector", LIST_OF(OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern()))) +
        " ?: ?" +
        OR(
          // start state defined for given action vector
          NAMED_GROUP("startState", OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern())) +
            " ?: ?" +
            OR(
              // end state and probability explicitly given
              NAMED_GROUP("endState", OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern())) +
                " ?: ?" + NAMED_GROUP("probability", PROBABILITY_PATTERN),
              // probability distribution given explicitly
              "\\R" + NAMED_GROUP("probabilityDistribution", LIST_OF(PROBABILITY_PATTERN))
            ),
          // uniform probability distribution
          "\\R" + NAMED_GROUP("probabilityUniformDistribution", UNIFORM.getPattern()),
          // end state = start state‚
          "\\R" + NAMED_GROUP("probabilityIdentityDistribution", IDENTITY.getPattern()),
          // matrix defined for given action vector
          "\\R" + NAMED_GROUP("probabilityMatrix", ROWS_OF(LIST_OF(PROBABILITY_PATTERN)))
        )
    );
  }

  public Map<State, Map<Vector<Action>, Map<State, Double>>> getTransitions() {
    return transitions;
  }

  public TransitionEntrySectionParser setStates(List<State> states) {
    this.states = states;
    return this;
  }

  public TransitionEntrySectionParser setAgentActions(List<List<Action>> agentActions) {
    this.agentActions = agentActions;
    return this;
  }

  public TransitionEntrySectionParser setTransitions(Map<State, Map<Vector<Action>, Map<State, Double>>> transitions) {
    this.transitions = transitions;
    return this;
  }

  public void parseSection(String section) {
    LOG.debug("Parsing 'T' section.");
    assertAllDependenciesSet();
    var match = getMatchOrThrow(section);
    if (!match.hasGroup("actionVector")) throw new ParsingFailedException("'T' section was parsed successfully, but actionVector is not present.");
    var actionVectors = CommonParser.parseActionVector(agentActions, match.getGroupAsStringOrThrow("actionVector"));
    if (match.hasGroup("startState")) {
      var startStates = CommonParser.parseStateOrWildcard(states, match.getGroupAsStringOrThrow("startState"));
      if (match.hasGroup("endState") && match.hasGroup("probability")) {
        var endStates = CommonParser.parseStateOrWildcard(states, match.getGroupAsStringOrThrow("endState"));
        var probability = match.getGroupAsDoubleOrThrow("probability");
        actionVectors.forEach(actionVector -> {
          startStates.forEach(startState -> {
            endStates.forEach(endState -> {
              saveTransitionRule(startState, actionVector, endState, probability);
            });
          });
        });
      } else if (match.hasGroup("probabilityDistribution")) {
        var probabilities = CommonParser.parseStatesAndTheirDistributions(states, match.getGroupAsStringOrThrow("probabilityDistribution"));
        actionVectors.forEach(actionVector -> {
          startStates.forEach(startState -> {
            saveTransitionRule(startState, actionVector, probabilities);
          });
        });
      }
    } else if (match.hasGroup("probabilityUniformDistribution")) {
      var startStates = states;
      var distribution = Distribution.createUniformDistribution(states);
      var probabilities = distribution.toMap();
      actionVectors.forEach(actionVector -> {
        startStates.forEach(startState -> {
          saveTransitionRule(startState, actionVector, probabilities);
        });
      });
    } else if (match.hasGroup("probabilityIdentityDistribution")) {
      var startStates = states;
      actionVectors.forEach(actionVector -> {
        startStates.forEach(startState -> {
          saveTransitionRule(startState, actionVector, startState, 1D);
        });
      });
    } else if (match.hasGroup("probabilityMatrix")) {
      var rawProbabilityDistributionRows = match.getGroupAsStringArrayOrThrow("probabilityMatrix", "\\R");

      for (int i = 0; i < rawProbabilityDistributionRows.length; i++) {
        var startState = states.get(i);
        var rawProbabilityDistribution = rawProbabilityDistributionRows[i];
        var probabilities = CommonParser.parseStatesAndTheirDistributions(states, rawProbabilityDistribution);

        actionVectors.forEach(actionVector -> {
          saveTransitionRule(startState, actionVector, probabilities);
        });
      }
    }
  }

  private void assertAllDependenciesSet() {

    if (states.isEmpty()) {
      throw new ParsingFailedException("'T' section was parsed, before 'states' have been initialized.");
    } else if (agentActions.isEmpty()) {
      throw new ParsingFailedException("'T' section was parsed, before 'actions' have been initialized.");
    }
  }

  private void saveTransitionRule(State start, Vector<Action> actionVector, State end, Double probability) {
    transitions.putIfAbsent(start, new HashMap<>());
    transitions.get(start).putIfAbsent(actionVector, new HashMap<>());
    if (transitions.get(start).get(actionVector).containsKey(end)) {
      transitions.get(start).put(actionVector, new HashMap<>());
    }
    transitions.get(start).get(actionVector).put(end, probability);
  }

  private void saveTransitionRule(State start, Vector<Action> actionVector, Map<State, Double> transitionProbabilities) {
    transitions.putIfAbsent(start, new HashMap<>());
    transitions.get(start).putIfAbsent(actionVector, transitionProbabilities);
  }
}
