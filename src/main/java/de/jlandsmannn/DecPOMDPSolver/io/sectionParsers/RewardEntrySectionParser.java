package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
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
import static de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPCommonKeyword.ANY;

public class RewardEntrySectionParser extends BaseSectionParser {

  private static final Logger LOG = LoggerFactory.getLogger(RewardEntrySectionParser.class);

  protected List<State> states = new ArrayList<>();
  protected List<List<Action>> agentActions = new ArrayList<>();
  protected List<List<Observation>> agentObservations = new ArrayList<>();
  protected Map<State, Map<Vector<Action>, Map<State, Map<Vector<Observation>, Double>>>> rewards = new HashMap<>();

  public RewardEntrySectionParser() {
    super(
      DPOMDPSectionKeyword.REWARD_ENTRY,
      DPOMDPSectionKeyword.REWARD_ENTRY + ": ?" +
        NAMED_GROUP("actionVector", LIST_OF(OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern()))) +
        " ?: ?" +
        NAMED_GROUP("startState", OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern())) +
        " ?: ?" +
        OR(
          NAMED_GROUP("endState", OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern())) +
            " ?: ?" +
            OR(
              NAMED_GROUP("observationVector", LIST_OF(OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern()))) +
                " ?: ?" + NAMED_GROUP("reward", NUMBER_PATTERN),
              "\n" + NAMED_GROUP("rewardDistribution", LIST_OF(NUMBER_PATTERN))
            ),
          // matrix defined for given action vector
          "\n" + NAMED_GROUP("rewardMatrix", ROWS_OF(LIST_OF(NUMBER_PATTERN)))
        )
    );
  }

  public Map<State, Map<Vector<Action>, Map<State, Map<Vector<Observation>, Double>>>> getRewards() {
    return rewards;
  }

  public RewardEntrySectionParser setStates(List<State> states) {
    this.states = states;
    return this;
  }

  public RewardEntrySectionParser setAgentActions(List<List<Action>> agentActions) {
    this.agentActions = agentActions;
    return this;
  }

  public RewardEntrySectionParser setAgentObservations(List<List<Observation>> agentObservations) {
    this.agentObservations = agentObservations;
    return this;
  }

  public RewardEntrySectionParser setRewards(Map<State, Map<Vector<Action>, Map<State, Map<Vector<Observation>, Double>>>> rewards) {
    this.rewards = rewards;
    return this;
  }

  public void parseSection(String section) {
    LOG.debug("Parsing 'R' section.");
    assertAllDependenciesSet();
    var match = getMatchOrThrow(section);
    if (!match.hasGroup("actionVector")) throw new ParsingFailedException("'R' section was parsed successfully, but actionVector is not present.");
    else if (!match.hasGroup("startState")) throw new ParsingFailedException("'R' section was parsed successfully, but startState is not present.");
    var actionVectors = CommonParser.parseActionVector(agentActions, match.getGroupAsStringOrThrow("actionVector"));
    var startStates = CommonParser.parseStateOrWildcard(states, match.getGroupAsStringOrThrow("startState"));
    if (match.hasGroup("endState")) {
      var endStates = CommonParser.parseStateOrWildcard(states, match.getGroupAsStringOrThrow("endState"));
      if (match.hasGroup("observationVector") && match.hasGroup("reward")) {
        var observationVectors = CommonParser.parseObservationVector(agentObservations, match.getGroupAsStringOrThrow("observationVector"));
        var reward = match.getGroupAsDoubleOrThrow("reward");
        startStates.forEach(startState -> {
          actionVectors.forEach(actionVector -> {
            endStates.forEach(endState -> {
              observationVectors.forEach(observationVector -> {
                saveRewardRule(startState, actionVector, endState, observationVector, reward);
              });
            });
          });
        });
      } else if (match.hasGroup("rewardDistribution")) {
        var rewards = CommonParser.parseObservationVectorsAndTheirDistributions(agentObservations, match.getGroupAsStringOrThrow("rewardDistribution"));
        startStates.forEach(startState -> {
          actionVectors.forEach(actionVector -> {
            endStates.forEach(endState -> {
              saveRewardRule(startState, actionVector, endState, rewards);
            });
          });
        });
      }
    } else if (match.hasGroup("rewardMatrix")) {
      var rawRewardDistributionRows = match.getGroupAsStringArrayOrThrow("rewardMatrix", "\n");

      for (int i = 0; i < rawRewardDistributionRows.length; i++) {
        var endState = states.get(i);
        var rawRewardsDistribution = rawRewardDistributionRows[i];
        var rewards = CommonParser.parseObservationVectorsAndTheirDistributions(agentObservations, rawRewardsDistribution);

        startStates.forEach(startState -> {
          actionVectors.forEach(actionVector -> {
            saveRewardRule(startState, actionVector, endState, rewards);
          });
        });
      }
    }
  }

  private void assertAllDependenciesSet() {
    if (states.isEmpty()) {
      throw new ParsingFailedException("'R' section was parsed, before 'states' have been initialized.");
    } else if (agentActions.isEmpty()) {
      throw new ParsingFailedException("'R' section was parsed, before 'actions' have been initialized.");
    } else if (agentObservations.isEmpty()) {
      throw new ParsingFailedException("'R' section was parsed, before 'observations' have been initialized.");
    }
  }

  private void saveRewardRule(State startState, Vector<Action> actionVector, State endState, Vector<Observation> observationVector, double reward) {
    rewards.putIfAbsent(startState, new HashMap<>());
    rewards.get(startState).putIfAbsent(actionVector, new HashMap<>());
    rewards.get(startState).get(actionVector).putIfAbsent(endState, new HashMap<>());
    rewards.get(startState).get(actionVector).get(endState).putIfAbsent(observationVector, reward);
  }

  private void saveRewardRule(State startState, Vector<Action> actionVector, State endState, Map<Vector<Observation>, Double> observationRewards) {
    rewards.putIfAbsent(startState, new HashMap<>());
    rewards.get(startState).putIfAbsent(actionVector, new HashMap<>());
    rewards.get(startState).get(actionVector).putIfAbsent(endState, new HashMap<>());
    rewards.get(startState).get(actionVector).get(endState).putAll(observationRewards);
  }

}
