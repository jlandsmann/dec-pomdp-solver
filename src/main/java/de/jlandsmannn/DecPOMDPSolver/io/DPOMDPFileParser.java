package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.AgentBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPRewardType;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

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
    }
    LOG.debug("No keyword matched current line, adding line to current section.");
    currentSectionBuilder.append(currentLine).append(System.lineSeparator());
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
    var match = DPOMDPSectionPattern.AGENTS
      .getMatch(section)
      .orElseThrow(() -> new IllegalArgumentException("Trying to parse 'agents' section, but found invalid format."));
    if (match.group("agentCount") != null) {
      var agentCountString = match.group("agentCount");
      var agentCount = Integer.parseInt(agentCountString);
      if (agentCount <= 0) throw new IllegalArgumentException("agentCount must be greater than zero.");
      LOG.debug("Found number of agents, creating {} agents with generic names.", agentCount);
      agentNames = IntStream.range(0, agentCount).mapToObj(i -> "A" + i).toList();
    } else if (match.group("agentNames") != null){
      var rawAgentNames = match.group("agentNames");
      agentNames = List.of(rawAgentNames.split(" "));
      LOG.debug("Found custom names of agents, creating {} agents with given names.", agentNames.size());
    } else {
      throw new IllegalStateException("'agents' section was parsed successfully, but neither agentCount nor agentNames are present.");
    }
  }

  protected void parseDiscount(String section) {
    LOG.debug("Parsing 'discount' section.");
    var match = DPOMDPSectionPattern.DISCOUNT
      .getMatch(section)
      .orElseThrow(() -> new IllegalArgumentException("Trying to parse discount section, but found invalid format."));
    if (match.group("discount") != null) {
      var discountString = match.group("discount");
      var discount = Double.parseDouble(discountString);
      LOG.debug("Found discount factor: {}", discount);
      if (discount < 0) throw new IllegalArgumentException("discount must be greater than or equal to zero.");
      else if (discount > 1) throw new IllegalArgumentException("discount must be less than or equal to one.");
      builder.setDiscountFactor(discount);
    } else {
      throw new IllegalStateException("'discount' section was parsed successfully, but discountFactor could not be found.");
    }
  }

  protected void parseRewardType(String section) {
    LOG.debug("Parsing 'value' section.");
    var match = DPOMDPSectionPattern.REWARD_TYPE
      .getMatch(section)
      .orElseThrow(() -> new IllegalArgumentException("Trying to parse value section, but found invalid format."));
    if (match.group("rewardType") != null) {
      var rewardTypeString = match.group("rewardType");
      rewardType = rewardTypeString.equals("cost") ? DPOMDPRewardType.COST : DPOMDPRewardType.REWARD;
      LOG.debug("Found reward type: {}", rewardType);
    } else {
      throw new IllegalStateException("'value' section was parsed successfully, but value could not be found.");
    }
  }

  protected void parseStates(String section) {
    LOG.debug("Parsing 'states' section.");
    var match = DPOMDPSectionPattern.STATES
      .getMatch(section)
      .orElseThrow(() -> new IllegalArgumentException("Trying to parse 'states' section, but found invalid format."));
    if (match.group("stateCount") != null) {
      var stateCountString = match.group("stateCount");
      var stateCount = Integer.parseInt(stateCountString);
      if (stateCount <= 0) throw new IllegalArgumentException("stateCount must be greater than zero.");
      LOG.debug("Found number of states, creating {} states with generic names.", stateCount);
      var states = IntStream.range(0, stateCount)
        .mapToObj(i -> "S" + i)
        .map(State::from)
        .toList();
      builder.addStates(states);
    } else if (match.group("stateNames") != null){
      var rawStateNames = match.group("stateNames");
      var states = State.listOf(rawStateNames.split(" "));
      builder.addStates(states);
      LOG.debug("Found custom names of states, creating {} states with given names.", states.size());
    } else {
      throw new IllegalStateException("'states' section was parsed successfully, but neither stateCount nor stateNames are present.");
    }
  }

  protected void parseStart(String section) {
    LOG.debug("Parsing 'start' section.");
    if (builder.getStates().isEmpty()) {
      throw new IllegalStateException("'start' section was parsed, before states have been initialized.");
    }
    var match = DPOMDPSectionPattern.START
      .getMatch(section)
      .orElseThrow(() -> new IllegalArgumentException("Trying to parse 'start' section, but found invalid format."));
    if (match.group("startState") != null) {
      var stateName = match.group("startState");
      var state = State.from(stateName);
      initialBeliefState = Distribution.createSingleEntryDistribution(state);
    }
    else if (match.group("startStateIndex") != null) {
      var rawStateIndex = match.group("startStateIndex");
      var stateIndex = Integer.parseInt(rawStateIndex);
      var state = builder.getStates().get(stateIndex);
      initialBeliefState = Distribution.createSingleEntryDistribution(state);
    }
    else if (match.group("uniformDistribution") != null) {
      var states = builder.getStates();
      initialBeliefState = Distribution.createUniformDistribution(states);
    }
    else if (match.group("distribution") != null) {
      var rawStateProbabilities = match.group("distribution");
      var rawDistribution = getStatesAndTheirProbabilities(rawStateProbabilities);
      initialBeliefState = Distribution.of(rawDistribution);
    }
    else if (match.group("includeStates") != null) {
      var rawStatesString = match.group("includeStates");
      var rawStates = rawStatesString.split(" ");
      var allStates = builder.getStates();
      var statesToInclude = Arrays.stream(rawStates).map(s -> {
        if (s.matches(CommonPattern.POSITIVE_INTEGER_PATTERN)) {
          var index = Integer.parseInt(s);
          return allStates.get(index);
        } else {
          return State.from(s);
        }
      }).toList();
      initialBeliefState = Distribution.createUniformDistribution(statesToInclude);
    }
    else if (match.group("excludeStates") != null) {
      var rawStatesString = match.group("excludeStates");
      var rawStates = rawStatesString.split(" ");
      var allStates = builder.getStates();
      var statesToExclude = Arrays.stream(rawStates).map(s -> {
        if (s.matches(CommonPattern.POSITIVE_INTEGER_PATTERN)) {
          var index = Integer.parseInt(s);
          return allStates.get(index);
        } else {
          return State.from(s);
        }
      }).toList();
      var statesToInclude = new ArrayList<>(allStates);
      statesToInclude.removeAll(statesToExclude);
      initialBeliefState = Distribution.createUniformDistribution(statesToInclude);
    }
  }

  private HashMap<State, Double> getStatesAndTheirProbabilities(String rawStateProbabilities) {
    var stateProbabilities = rawStateProbabilities.split(" ");
    if (stateProbabilities.length > builder.getStates().size()) {
      throw new IllegalArgumentException("Distribution of start states consists of more states than defined.");
    }
    var rawDistribution = new HashMap<State, Double>();
    for (int i = 0; i < stateProbabilities.length; i++) {
      var state = builder.getStates().get(i);
      var probability = Double.parseDouble(stateProbabilities[i]);
      rawDistribution.put(state, probability);
    }
    return rawDistribution;
  }

  protected void parseActions(String section) {
    LOG.debug("Parsing 'actions' section.");
    if (agentNames.isEmpty()) {
      throw new IllegalStateException("'actions' section was parsed, before agents have been initialized.");
    }

    var match = DPOMDPSectionPattern.ACTIONS
      .getMatch(section)
      .orElseThrow(() -> new IllegalArgumentException("Trying to parse 'actions' section, but found invalid format."));
    var rawActions = match.group("agentActions");
    var rawActionsPerAgent = rawActions.split("\n");
    if (rawActionsPerAgent.length != agentNames.size()) {
      throw new IllegalArgumentException("'actions' does not have same number of agents as 'agents' section.");
    }
    for (int i = 0; i < rawActionsPerAgent.length; i++) {
      var rawActionsForAgent = rawActionsPerAgent[i];
      var agentName = agentNames.get(i);
      if (rawActionsForAgent.matches(CommonPattern.POSITIVE_INTEGER_PATTERN)) {
        var numberOfActions = Integer.parseInt(rawActionsForAgent);
        var actions = IntStream.range(0, numberOfActions).mapToObj(idx -> agentName + "-A" + idx).map(Action::from).toList();
        agentActions.add(i, actions);
      } else {
        var actionNames = rawActionsForAgent.split(" ");
        var actions = Action.listOf(actionNames);
        agentActions.add(i, actions);
      }
    }
  }

  protected void parseObservations(String section) {
    LOG.debug("Parsing 'observations' section.");
    if (agentNames.isEmpty()) {
      throw new IllegalStateException("'observations' section was parsed, before 'agents' have been initialized.");
    } else if (agentActions.isEmpty()) {
      throw new IllegalStateException("'observations' section was parsed, before 'actions' have been initialized.");
    }

    var match = DPOMDPSectionPattern.OBSERVATIONS
      .getMatch(section)
      .orElseThrow(() -> new IllegalArgumentException("Trying to parse 'observations' section, but found invalid format."));
    var rawObservations = match.group("agentObservations");
    var rawObservationsPerAgent = rawObservations.split("\n");
    if (rawObservationsPerAgent.length != agentNames.size()) {
      throw new IllegalArgumentException("'observations' does not have same number of agents as 'agents' section.");
    }
    for (int i = 0; i < rawObservationsPerAgent.length; i++) {
      var rawObservationsForAgent = rawObservationsPerAgent[i];
      var agentName = agentNames.get(i);
      if (rawObservationsForAgent.matches(CommonPattern.POSITIVE_INTEGER_PATTERN)) {
        var numberOfObservations = Integer.parseInt(rawObservationsForAgent);
        var observations = IntStream.range(0, numberOfObservations).mapToObj(idx -> agentName + "-O" + idx).map(Observation::from).toList();
        agentObservations.add(i, observations);
      } else {
        var observationNames = rawObservationsForAgent.split(" ");
        var observations = Observation.listOf(observationNames);
        agentObservations.add(i, observations);
      }
    }
  }

  protected void parseTransitionEntry(String section) {
    LOG.debug("Parsing 'T' section.");

  }

  protected void parseRewardEntry(String section) {
    LOG.debug("Parsing 'R' section.");
  }

  protected void parseObservationEntry(String section) {
    LOG.debug("Parsing 'O' section.");
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
