package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardEntryParser {

  private static final Logger LOG = LoggerFactory.getLogger(RewardEntryParser.class);

  protected List<State> states = new ArrayList<>();
  protected List<List<Action>> agentActions = new ArrayList<>();
  protected List<List<Observation>> agentObservations = new ArrayList<>();
  protected Map<State, Map<Vector<Action>, Map<State, Map<Vector<Observation>, Double>>>> rewards = new HashMap<>();

  public Map<State, Map<Vector<Action>, Map<State, Map<Vector<Observation>, Double>>>> getRewards() {
    return rewards;
  }

  public RewardEntryParser setStates(List<State> states) {
    this.states = states;
    return this;
  }

  public RewardEntryParser setAgentActions(List<List<Action>> agentActions) {
    this.agentActions = agentActions;
    return this;
  }

  public RewardEntryParser setAgentObservations(List<List<Observation>> agentObservations) {
    this.agentObservations = agentObservations;
    return this;
  }

  public void parseRewardEntry(String section) {
    LOG.debug("Parsing 'R' section.");
    if (states.isEmpty()) {
      throw new IllegalStateException("'R' section was parsed, before 'states' have been initialized.");
    } else if (agentActions.isEmpty()) {
      throw new IllegalStateException("'R' section was parsed, before 'actions' have been initialized.");
    } else if (agentObservations.isEmpty()) {
      throw new IllegalStateException("'R' section was parsed, before 'observations' have been initialized.");
    }
    var match = DPOMDPSectionPattern.REWARD_ENTRY
      .getMatch(section)
      .orElseThrow(() -> new IllegalArgumentException("Trying to parse 'R' section, but found invalid format."));
    if (match.group("actionVector") == null) throw new IllegalStateException("'R' section was parsed successfully, but actionVector is not present.");
    if (match.group("startState") == null) throw new IllegalStateException("'R' section was parsed successfully, but startState is not present.");
    var actionVectors = CommonParser.parseActionVector(agentActions, match.group("actionVector"));
    var startStates = CommonParser.parseStateOrWildcard(states, match.group("startState"));
    if (match.group("endState") != null) {
      var endStates = CommonParser.parseStateOrWildcard(states, match.group("endState"));
      if (match.group("observationVector") != null && match.group("reward") != null) {
        var observationVectors = CommonParser.parseObservationVector(agentObservations, match.group("observationVector"));
        var reward = Double.parseDouble(match.group("reward"));
        startStates.forEach(startState -> {
          actionVectors.forEach(actionVector -> {
            endStates.forEach(endState -> {
              observationVectors.forEach(observationVector -> {
                saveRewardRule(startState, actionVector, endState, observationVector, reward);
              });
            });
          });
        });
      } else if (match.group("rewardDistribution") != null) {
        var rewards = CommonParser.parseObservationVectorsAndTheirDistributions(agentObservations, match.group("rewardDistribution"));
        startStates.forEach(startState -> {
          actionVectors.forEach(actionVector -> {
            endStates.forEach(endState -> {
              saveRewardRule(startState, actionVector, endState, rewards);
            });
          });
        });
      }
    } else if (match.group("rewardMatrix") != null) {
      var rawRewardDistributionRows = match.group("rewardMatrix").split("\n");

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
