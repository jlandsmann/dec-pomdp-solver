package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransitionEntryParser {
  private static final Logger LOG = LoggerFactory.getLogger(TransitionEntryParser.class);

  protected List<State> states = new ArrayList<>();
  protected List<List<Action>> agentActions = new ArrayList<>();
  protected Map<State, Map<Vector<Action>, Map<State, Double>>> transitions = new HashMap<>();

  public Map<State, Map<Vector<Action>, Map<State, Double>>> getTransitions() {
    return transitions;
  }

  public TransitionEntryParser setStates(List<State> states) {
    this.states = states;
    return this;
  }

  public TransitionEntryParser setAgentActions(List<List<Action>> agentActions) {
    this.agentActions = agentActions;
    return this;
  }

  public TransitionEntryParser setTransitions(Map<State, Map<Vector<Action>, Map<State, Double>>> transitions) {
    this.transitions = transitions;
    return this;
  }

  public void parseTransitionEntry(String section) {
    LOG.debug("Parsing 'T' section.");
    if (states.isEmpty()) {
      throw new IllegalStateException("'T' section was parsed, before 'states' have been initialized.");
    } else if (agentActions.isEmpty()) {
      throw new IllegalStateException("'T' section was parsed, before 'actions' have been initialized.");
    }
    var match = DPOMDPSectionPattern.TRANSITION_ENTRY
      .getMatch(section)
      .orElseThrow(() -> new IllegalArgumentException("Trying to parse 'T' section, but found invalid format."));
    if (match.group("actionVector") == null) throw new IllegalStateException("'T' section was parsed successfully, but actionVector is not present.");
    var actionVectors = CommonParser.parseActionVector(agentActions, match.group("actionVector"));
    if (match.group("startState") != null) {
      var startStates = CommonParser.parseStateOrWildcard(states, match.group("startState"));
      if (match.group("endState") != null && match.group("probability") != null) {
        var endStates = CommonParser.parseStateOrWildcard(states, match.group("endState"));
        var probability = Double.parseDouble(match.group("probability"));
        actionVectors.forEach(actionVector -> {
          startStates.forEach(startState -> {
            endStates.forEach(endState -> {
              saveTransitionRule(startState, actionVector, endState, probability);
            });
          });
        });
      } else if (match.group("probabilityDistribution") != null) {
        var probabilities = CommonParser.parseStatesAndTheirDistributions(states, match.group("probabilityDistribution"));
        actionVectors.forEach(actionVector -> {
          startStates.forEach(startState -> {
            saveTransitionRule(startState, actionVector, probabilities);
          });
        });
      }
    } else if (match.group("probabilityUniformDistribution") != null) {
      var startStates = states;
      var distribution = Distribution.createUniformDistribution(states);
      var probabilities = distribution.toMap();
      actionVectors.forEach(actionVector -> {
        startStates.forEach(startState -> {
          saveTransitionRule(startState, actionVector, probabilities);
        });
      });
    } else if (match.group("probabilityIdentityDistribution") != null) {
      var startStates = states;
      actionVectors.forEach(actionVector -> {
        startStates.forEach(startState -> {
          saveTransitionRule(startState, actionVector, startState, 1D);
        });
      });
    } else if (match.group("probabilityMatrix") != null) {
      var rawProbabilityDistributionRows = match.group("probabilityMatrix").split("\n");

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
