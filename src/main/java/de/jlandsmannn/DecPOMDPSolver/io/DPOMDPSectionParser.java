package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.AgentBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorCombinationBuilder;
import de.jlandsmannn.DecPOMDPSolver.io.sectionParsers.*;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPRewardType;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DPOMDPSectionParser {
  private static final Logger LOG = LoggerFactory.getLogger(DPOMDPFileParser.class);

  protected DecPOMDPBuilder builder;

  protected List<String> agentNames = new ArrayList<>();
  protected List<List<Action>> agentActions = new ArrayList<>();
  protected List<List<Observation>> agentObservations = new ArrayList<>();
  protected DPOMDPRewardType rewardType = DPOMDPRewardType.REWARD;
  protected Map<State, Map<Vector<Action>, Map<State, Double>>> transitions = new HashMap<>();
  protected Map<State, Map<Vector<Action>, Map<State, Map<Vector<Observation>, Double>>>> rewards = new HashMap<>();
  protected Map<Vector<Action>, Map<State, Map<Vector<Observation>, Double>>> observations = new HashMap<>();

  public DPOMDPSectionParser(DecPOMDPBuilder builder) {
    this.builder = builder;
  }

  public void parseSection(DPOMDPSectionKeyword keyword, String section) {
    switch (keyword) {
      case DPOMDPSectionKeyword.AGENTS:
        parseAgents(section);
        break;
      case DPOMDPSectionKeyword.DISCOUNT:
        parseDiscount(section);
        break;
      case DPOMDPSectionKeyword.REWARD_TYPE:
        parseRewardType(section);
        break;
      case DPOMDPSectionKeyword.STATES:
        parseStates(section);
        break;
      case DPOMDPSectionKeyword.START:
        parseStart(section);
        break;
      case DPOMDPSectionKeyword.ACTIONS:
        parseActions(section);
        break;
      case DPOMDPSectionKeyword.OBSERVATIONS:
        parseObservations(section);
        break;
      case DPOMDPSectionKeyword.TRANSITION_ENTRY:
        parseTransitionEntry(section);
        break;
      case DPOMDPSectionKeyword.REWARD_ENTRY:
        parseRewardEntry(section);
        break;
      case DPOMDPSectionKeyword.OBSERVATION_ENTRY:
        parseObservationEntry(section);
        break;
    }
  }

  public DPOMDPSectionParser gatherData() {
    gatherDataAndAddToBuilder();
    return this;
  }

  public DecPOMDPBuilder getBuilder() {
    return builder;
  }

  protected void parseAgents(String section) {
    LOG.debug("Parsing 'agents' section.");
    var parser = new AgentsSectionParser();
    parser.parseSection(section);
    agentNames = parser.getAgentNames();
  }

  protected void parseDiscount(String section) {
    LOG.debug("Parsing 'discount' section.");
    var parser = new DiscountSectionParser();
    parser.parseSection(section);
    builder.setDiscountFactor(parser.getDiscountFactor());
  }

  protected void parseRewardType(String section) {
    LOG.debug("Parsing 'value' section.");
    var parser = new RewardTypeSectionParser();
    parser.parseSection(section);
    rewardType = parser.getRewardType();
  }

  protected void parseStates(String section) {
    LOG.debug("Parsing 'states' section.");
    var parser = new StatesSectionParser();
    parser.parseSection(section);
    builder.addStates(parser.getStates());
  }

  protected void parseStart(String section) {
    LOG.debug("Parsing 'start' section.");
    var states = builder.getStates();
    var parser = new StartSectionParser();
    parser
      .setStates(states)
      .parseSection(section);
    builder.setInitialBeliefState(parser.getInitialBeliefState());
  }

  protected void parseActions(String section) {
    LOG.debug("Parsing 'actions' section.");
    var parser = new ActionsSectionParser();
    parser
      .setAgentNames(agentNames)
      .parseSection(section);
    agentActions = parser.getAgentActions();
  }

  protected void parseObservations(String section) {
    LOG.debug("Parsing 'observations' section.");
    var parser = new ObservationsSectionParser();
    parser
      .setAgentNames(agentNames)
      .parseSection(section);
    agentObservations = parser.getAgentObservations();
  }

  protected void parseTransitionEntry(String section) {
    LOG.debug("Parsing 'T' section.");
    var states = builder.getStates();
    var parser = new TransitionEntrySectionParser();
    parser
      .setStates(states)
      .setAgentActions(agentActions)
      .setTransitions(transitions)
      .parseSection(section);
    transitions = parser.getTransitions();
  }

  protected void parseObservationEntry(String section) {
    LOG.debug("Parsing 'O' section.");
    var states = builder.getStates();
    var parser = new ObservationEntrySectionParser();
    parser
      .setStates(states)
      .setAgentActions(agentActions)
      .setAgentObservations(agentObservations)
      .setObservations(observations)
      .parseSection(section);
    observations = parser.getObservations();
  }

  protected void parseRewardEntry(String section) {
    LOG.debug("Parsing 'R' section.");
    var states = builder.getStates();
    var parser = new RewardEntrySectionParser();
    parser
      .setStates(states)
      .setAgentActions(agentActions)
      .setAgentObservations(agentObservations)
      .setRewards(rewards)
      .parseSection(section);
    rewards = parser.getRewards();
  }

  protected void gatherDataAndAddToBuilder() {
    gatherAgentsAndAddToBuilder();
    gatherTransitionsAndAddToBuilder();
    gatherObservationsAndAddToBuilder();
    gatherRewardsAndAddToBuilder();
  }

  private void gatherAgentsAndAddToBuilder() {
    for (int i = 0; i < agentNames.size(); i++) {
      var name = agentNames.get(i);
      var actions = agentActions.get(i);
      var observations = agentObservations.get(i);
      var agent = new AgentBuilder()
        .setName(name)
        .setActions(actions)
        .setObservations(observations)
        .createAgent();
      builder.addAgent(agent);
    }
  }

  private void gatherTransitionsAndAddToBuilder() {
    var actionCombinations = VectorCombinationBuilder.listOf(agentActions);
    for (var state : builder.getStates()) {
      var actionMap = transitions.get(state);
      if (actionMap == null) throw new IllegalStateException("State " + state + " has no transitions.");
      for (var actionVector : actionCombinations) {
        var transitionMap = actionMap.get(actionVector);
        if (transitionMap == null) throw new IllegalStateException("State " + state + " with actions " + actionVector + " has no transitions.");
        var distribution = Distribution.of(transitionMap);
        builder.addTransition(state, actionVector, distribution);
      }
    }
  }

  private void gatherObservationsAndAddToBuilder() {
    var actionCombinations = VectorCombinationBuilder.listOf(agentActions);

    for (var actionVector : actionCombinations) {
      var stateMap = observations.get(actionVector);
      if (stateMap == null) throw new IllegalStateException("Actions " + actionVector + " have no observations.");
      for (var state : builder.getStates()) {
        var observationMap = stateMap.get(state);
        if (observationMap == null) throw new IllegalStateException("State " + state + " has no observations for actions " + actionVector + ".");
        var distribution = Distribution.of(observationMap);
        builder.addObservation(actionVector, state, distribution);
      }
    }
  }

  private void gatherRewardsAndAddToBuilder() {
    var actionCombinations = VectorCombinationBuilder.listOf(agentActions);
    var observationCombination = VectorCombinationBuilder.listOf(agentObservations);

    for (var state : builder.getStates()) {
      var actionMap = rewards.getOrDefault(state, Map.of());
      for (var actionVector : actionCombinations) {
        var transitionMap = actionMap.getOrDefault(actionVector, Map.of());
        for (var followState : builder.getStates()) {
          var observationMap = transitionMap.getOrDefault(followState, Map.of());
          for (var observationVector : observationCombination) {
            var reward = observationMap.getOrDefault(observationVector, 0D);
            if (rewardType == DPOMDPRewardType.COST) reward *= -1;
            builder.addReward(state, actionVector, reward);
          }

        }
      }
    }
  }

}
