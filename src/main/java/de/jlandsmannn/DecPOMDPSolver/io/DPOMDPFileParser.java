package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.AgentBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorStreamBuilder;
import de.jlandsmannn.DecPOMDPSolver.io.sectionParsers.*;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPRewardType;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DPOMDPFileParser {
  private static final Logger LOG = LoggerFactory.getLogger(DPOMDPFileParser.class);

  protected final DecPOMDPBuilder builder;

  protected DPOMDPSectionKeyword currentKeyword = DPOMDPSectionKeyword.COMMENT;
  protected StringBuilder currentSectionBuilder = new StringBuilder();

  protected List<String> agentNames = new ArrayList<>();
  protected List<List<Action>> agentActions = new ArrayList<>();
  protected List<List<Observation>> agentObservations = new ArrayList<>();
  protected DPOMDPRewardType rewardType = DPOMDPRewardType.REWARD;
  protected Map<State, Map<Vector<Action>, Map<State, Double>>> transitions = new HashMap<>();
  protected Map<State, Map<Vector<Action>, Map<State, Map<Vector<Observation>, Double>>>> rewards = new HashMap<>();
  protected Map<Vector<Action>, Map<State, Map<Vector<Observation>, Double>>> observations = new HashMap<>();

  public DPOMDPFileParser() {
    this(new DecPOMDPBuilder());
  }

  public DPOMDPFileParser(DecPOMDPBuilder builder) {
    this.builder = builder;
  }

  public static Optional<DecPOMDPBuilder> parseDecPOMDP(String fileName) {
    try {
      var parser = new DPOMDPFileParser();
      var decPOMDP = parser.doParseDecPOMDP(fileName);
      return Optional.of(decPOMDP);
    } catch (Exception e) {
      LOG.warn("Could not parse decPOMDP file: {}", fileName, e);
      return Optional.empty();
    }
  }

  protected DecPOMDPBuilder doParseDecPOMDP(String fileName) throws IOException {
    try (var file = readFile(fileName)) {
      String currentLine = null;
      do {
        currentLine = file.readLine();
        parseLine(currentLine);
      } while (currentLine != null);
    }
    gatherDataAndAddToBuilder();
    return builder;
  }

  protected BufferedReader readFile(String fileName) throws IOException {
    var classLoader = getClass().getClassLoader();
    var url = classLoader.getResource(fileName);
    if (url == null) throw new FileNotFoundException(fileName);
    var path = Path.of(url.getPath());
    return Files.newBufferedReader(path);
  }

  protected void parseLine(String currentLine) {
    if (currentLine == null) {
      LOG.info("Reached end of file, finishing current section and parsing it.");
      parseCurrentSection();
      return;
    } else if (DPOMDPSectionKeyword.COMMENT.isAtBeginningOf(currentLine)) {
      LOG.debug("Found comment line, ignoring it.");
      return;
    }
    var keywordMatching = DPOMDPSectionKeyword.ALL
      .stream()
      .filter(keyword -> keyword.isAtBeginningOf(currentLine))
      .findFirst();

    if (keywordMatching.isPresent()) {
      LOG.debug("Found keyword at beginning of line, finishing current section and parsing it.");
      parseCurrentSection();
      startNewSection(keywordMatching.get());
      currentSectionBuilder.append(currentLine.trim());
      return;
    }
    LOG.debug("No keyword matched current line, adding line to current section.");
    currentSectionBuilder.append(System.lineSeparator()).append(currentLine.trim());
  }

  protected void parseCurrentSection() {
    parseSection(currentKeyword, currentSectionBuilder.toString());
  }

  protected void startNewSection(DPOMDPSectionKeyword keyword) {
    currentSectionBuilder = new StringBuilder();
    currentKeyword = keyword;
  }

  protected void parseSection(DPOMDPSectionKeyword keyword, String section) {
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

  protected void parseAgents(String section) {
    LOG.debug("Parsing 'agents' section.");
    var parser = new AgentParser();
    parser.parseAgents(section);
    agentNames = parser.getAgentNames();
  }

  protected void parseDiscount(String section) {
    LOG.debug("Parsing 'discount' section.");
    var parser = new DiscountParser();
    parser.parseDiscount(section);
    builder.setDiscountFactor(parser.getDiscountFactor());
  }

  protected void parseRewardType(String section) {
    LOG.debug("Parsing 'value' section.");
    var parser = new RewardTypeParser();
    parser.parseRewardType(section);
    rewardType = parser.getRewardType();
  }

  protected void parseStates(String section) {
    LOG.debug("Parsing 'states' section.");
    var parser = new StatesParser();
    parser.parseStates(section);
    builder.addStates(parser.getStates());
  }

  protected void parseStart(String section) {
    LOG.debug("Parsing 'start' section.");
    var states = builder.getStates();
    var parser = new StartParser();
    parser
      .setStates(states)
      .parseStart(section);
    builder.setInitialBeliefState(parser.getInitialBeliefState());
  }

  protected void parseActions(String section) {
    LOG.debug("Parsing 'actions' section.");
    var parser = new ActionsParser();
    parser
      .setAgentNames(agentNames)
      .parseActions(section);
    agentActions = parser.getAgentActions();
  }

  protected void parseObservations(String section) {
    LOG.debug("Parsing 'observations' section.");
    var parser = new ObservationsParser();
    parser
      .setAgentNames(agentNames)
      .parseObservations(section);
    agentObservations = parser.getAgentObservations();
  }

  protected void parseTransitionEntry(String section) {
    LOG.debug("Parsing 'T' section.");
    var states = builder.getStates();
    var parser = new TransitionEntryParser();
    parser
      .setStates(states)
      .setAgentActions(agentActions)
      .setTransitions(transitions)
      .parseTransitionEntry(section);
    transitions = parser.getTransitions();
  }

  protected void parseObservationEntry(String section) {
    LOG.debug("Parsing 'O' section.");
    var states = builder.getStates();
    var parser = new ObservationEntryParser();
    parser
      .setStates(states)
      .setAgentActions(agentActions)
      .setAgentObservations(agentObservations)
      .setObservations(observations)
      .parseObservationEntry(section);
    observations = parser.getObservations();
  }

  protected void parseRewardEntry(String section) {
    LOG.debug("Parsing 'R' section.");
    var states = builder.getStates();
    var parser = new RewardEntryParser();
    parser
      .setStates(states)
      .setAgentActions(agentActions)
      .setAgentObservations(agentObservations)
      .setRewards(rewards)
      .parseRewardEntry(section);
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
      var agent = new AgentBuilder().setName(name).setActions(actions).setObservations(observations).createAgent();
      builder.addAgent(agent);
    }
  }

  private void gatherTransitionsAndAddToBuilder() {
    var actionCombinations = VectorStreamBuilder.forEachCombination(agentActions).toList();
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
    var actionCombinations = VectorStreamBuilder.forEachCombination(agentActions).toList();

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
    var actionCombinations = VectorStreamBuilder.forEachCombination(agentActions).toList();
    var observationCombination = VectorStreamBuilder.forEachCombination(agentObservations).toList();

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
