package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.AgentBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.io.sectionParsers.*;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPRewardType;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  protected Distribution<State> initialBeliefState;
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
      return Optional.empty();
    }
  }

  protected DecPOMDPBuilder doParseDecPOMDP(String fileName) throws IOException {
    var path = Path.of(fileName);
    try (var file = Files.newBufferedReader(path)) {
      var currentLine = file.readLine();
      do {
        currentLine = file.readLine();
        parseLine(currentLine);
      } while (currentLine != null);
    }
    gatherAgentsAndAddToBuilder();
    return builder;
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
      case DPOMDPSectionKeyword.AGENTS: parseAgents(section);
      case DPOMDPSectionKeyword.DISCOUNT: parseDiscount(section);
      case DPOMDPSectionKeyword.REWARD_TYPE: parseRewardType(section);
      case DPOMDPSectionKeyword.STATES: parseStates(section);
      case DPOMDPSectionKeyword.START: parseStart(section);
      case DPOMDPSectionKeyword.ACTIONS: parseActions(section);
      case DPOMDPSectionKeyword.OBSERVATIONS: parseObservations(section);
      case DPOMDPSectionKeyword.TRANSITION_ENTRY: parseTransitionEntry(section);
      case DPOMDPSectionKeyword.REWARD_ENTRY: parseRewardEntry(section);
      case DPOMDPSectionKeyword.OBSERVATION_ENTRY: parseObservationEntry(section);
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
    initialBeliefState = parser.getInitialBeliefState();
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
      .parseRewardEntry(section);
    rewards = parser.getRewards();
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
}
