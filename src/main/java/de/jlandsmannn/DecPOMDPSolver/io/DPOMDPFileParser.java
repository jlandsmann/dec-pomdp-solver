package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.AgentBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorStreamBuilder;
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
import java.util.stream.Collectors;
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
      var rawDistribution = parseStatesAndTheirDistributions(rawStateProbabilities);
      initialBeliefState = Distribution.of(rawDistribution);
    }
    else if (match.group("includeStates") != null) {
      var rawStatesString = match.group("includeStates");
      var rawStates = rawStatesString.split(" ");
      var allStates = builder.getStates();
      var statesToInclude = Arrays.stream(rawStates).map(s -> {
        if (s.matches(CommonPattern.INDEX_PATTERN)) {
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
        if (s.matches(CommonPattern.INDEX_PATTERN)) {
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
      if (rawActionsForAgent.matches(CommonPattern.INDEX_PATTERN)) {
        var numberOfActions = Integer.parseInt(rawActionsForAgent);
        if (numberOfActions == 0) throw new IllegalArgumentException("Number of actions must be greater than 0.");
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
      if (rawObservationsForAgent.matches(CommonPattern.INDEX_PATTERN)) {
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
    if (builder.getStates().isEmpty()) {
      throw new IllegalStateException("'T' section was parsed, before 'states' have been initialized.");
    } else if (agentActions.isEmpty()) {
      throw new IllegalStateException("'T' section was parsed, before 'actions' have been initialized.");
    }
    var match = DPOMDPSectionPattern.TRANSITION_ENTRY
      .getMatch(section)
      .orElseThrow(() -> new IllegalArgumentException("Trying to parse 'T' section, but found invalid format."));
    if (match.group("actionVector") == null) throw new IllegalStateException("'T' section was parsed successfully, but actionVector is not present.");
    var actionVectors = parseActionVector(match.group("actionVector"));
    if (match.group("startState") != null) {
      var startStates = parseStateOrWildcard(match.group("startState"));
      if (match.group("endState") != null && match.group("probability") != null) {
        var endStates = parseStateOrWildcard(match.group("endState"));
        var probability = Double.parseDouble(match.group("probability"));
        actionVectors.forEach(actionVector -> {
          startStates.forEach(startState -> {
            endStates.forEach(endState -> {
              saveTransitionRule(startState, actionVector, endState, probability);
            });
          });
        });
      } else if (match.group("probabilityDistribution") != null) {
        var probabilities = parseStatesAndTheirDistributions(match.group("probabilityDistribution"));
        actionVectors.forEach(actionVector -> {
          startStates.forEach(startState -> {
            saveTransitionRule(startState, actionVector, probabilities);
          });
        });
      }
    } else if (match.group("probabilityUniformDistribution") != null) {
      var startStates = builder.getStates();
      var distribution = Distribution.createUniformDistribution(builder.getStates());
      var probabilities = distribution.toMap();
      actionVectors.forEach(actionVector -> {
        startStates.forEach(startState -> {
          saveTransitionRule(startState, actionVector, probabilities);
        });
      });
    } else if (match.group("probabilityIdentityDistribution") != null) {
      var startStates = builder.getStates();
      actionVectors.forEach(actionVector -> {
        startStates.forEach(startState -> {
          saveTransitionRule(startState, actionVector, startState, 1D);
        });
      });
    } else if (match.group("probabilityMatrix") != null) {
      var rawProbabilityDistributionRows = match.group("probabilityMatrix").split("\n");

      for (int i = 0; i < rawProbabilityDistributionRows.length; i++) {
        var startState = builder.getStates().get(i);
        var rawProbabilityDistribution = rawProbabilityDistributionRows[i];
        var probabilities = parseStatesAndTheirDistributions(rawProbabilityDistribution);

        actionVectors.forEach(actionVector -> {
          saveTransitionRule(startState, actionVector, probabilities);
        });
      }
    }
  }

  protected void parseObservationEntry(String section) {
    LOG.debug("Parsing 'O' section.");
    if (builder.getStates().isEmpty()) {
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
    var actionVectors = parseActionVector(match.group("actionVector"));
    if (match.group("endState") != null) {
      var endStates = parseStateOrWildcard(match.group("endState"));
      if (match.group("observationVector") != null && match.group("probability") != null) {
        var observationVectors = parseObservationVector(match.group("observationVector"));
        var probability = Double.parseDouble(match.group("probability"));
        actionVectors.forEach(actionVector -> {
          endStates.forEach(startState -> {
            observationVectors.forEach(observationVector -> {
              saveObservationRule(actionVector, startState, observationVector, probability);
            });
          });
        });
      } else if (match.group("probabilityDistribution") != null) {
        var probabilities = parseObservationVectorsAndTheirDistributions(match.group("probabilityDistribution"));
        actionVectors.forEach(actionVector -> {
          endStates.forEach(endState -> {
            saveObservationRule(actionVector, endState, probabilities);
          });
        });
      }
    } else if (match.group("probabilityUniformDistribution") != null) {
      var endStates = builder.getStates();
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
        var endState = builder.getStates().get(i);
        var rawProbabilityDistribution = rawProbabilityDistributionRows[i];
        var probabilities = parseObservationVectorsAndTheirDistributions(rawProbabilityDistribution);

        actionVectors.forEach(actionVector -> {
          saveObservationRule(actionVector, endState, probabilities);
        });
      }
    }
  }

  protected void parseRewardEntry(String section) {
    LOG.debug("Parsing 'R' section.");
    if (builder.getStates().isEmpty()) {
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
    var actionVectors = parseActionVector(match.group("actionVector"));
    var startStates = parseStateOrWildcard(match.group("startState"));
    if (match.group("endState") != null) {
      var endStates = parseStateOrWildcard(match.group("endState"));
      if (match.group("observationVector") != null && match.group("reward") != null) {
        var observationVectors = parseObservationVector(match.group("observationVector"));
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
        var rewards = parseObservationVectorsAndTheirDistributions(match.group("rewardDistribution"));
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
        var endState = builder.getStates().get(i);
        var rawRewardsDistribution = rawRewardDistributionRows[i];
        var rewards = parseObservationVectorsAndTheirDistributions(rawRewardsDistribution);

        startStates.forEach(startState -> {
          actionVectors.forEach(actionVector -> {
            saveRewardRule(startState, actionVector, endState, rewards);
          });
        });
      }
    }
  }

  private Map<State, Double> parseStatesAndTheirDistributions(String rawStateProbabilities) {
    var stateProbabilities = rawStateProbabilities.split(" ");
    if (stateProbabilities.length > builder.getStates().size()) {
      throw new IllegalArgumentException("Distribution of states consists of more states than defined.");
    }
    var rawDistribution = new HashMap<State, Double>();
    for (int i = 0; i < stateProbabilities.length; i++) {
      var state = builder.getStates().get(i);
      var probability = Double.parseDouble(stateProbabilities[i]);
      rawDistribution.put(state, probability);
    }
    return rawDistribution;
  }

  private Map<Vector<Observation>, Double> parseObservationVectorsAndTheirDistributions(String rawProbabilities) {
    var observationCombinations = VectorStreamBuilder.forEachCombination(agentObservations).toList();
    var probabilities = rawProbabilities.split(" ");
    if (probabilities.length > observationCombinations.size()) {
      throw new IllegalArgumentException("Distribution of observation vectors consists of more vectors than defined.");
    }
    var rawDistribution = new HashMap<Vector<Observation>, Double>();
    for (int i = 0; i < probabilities.length; i++) {
      var vector = observationCombinations.get(i);
      var probability = Double.parseDouble(probabilities[i]);
      rawDistribution.put(vector, probability);
    }
    return rawDistribution;
  }

  private List<Vector<Action>> parseActionVector(String rawActionVector) {
    if (rawActionVector.equals("*")) {
      return VectorStreamBuilder.forEachCombination(agentActions).toList();
    }
    var actions = rawActionVector.split(" ");
    var listOfActions = new ArrayList<List<Action>>();
    for (int i = 0; i < actions.length; i++) {
      var actionName = actions[i];
      var possibleActions = agentActions.get(i);
      if (actionName.equals("*")) {
        listOfActions.add(i, possibleActions);
      } else if (possibleActions.contains(Action.from(actionName))) {
        listOfActions.add(i, Action.listOf(actionName));
      };
    }
    return VectorStreamBuilder.forEachCombination(listOfActions).toList();
  }

  private List<Vector<Observation>> parseObservationVector(String rawObservationVector) {
    if (rawObservationVector.equals("*")) {
      return VectorStreamBuilder.forEachCombination(agentObservations).toList();
    }
    var observations = rawObservationVector.split(" ");
    var listOfObservations = new ArrayList<List<Observation>>();
    for (int i = 0; i < observations.length; i++) {
      var observationName = observations[i];
      var possibleObservations = agentObservations.get(i);
      if (observationName.equals("*")) {
        listOfObservations.add(i, possibleObservations);
      } else if (possibleObservations.contains(Observation.from(observationName))) {
        listOfObservations.add(i, Observation.listOf(observationName));
      };
    }
    return VectorStreamBuilder.forEachCombination(listOfObservations).toList();
  }

  private List<State> parseStateOrWildcard(String rawStateString) {
    var allStates = builder.getStates();
    var state = State.from(rawStateString);
    if (rawStateString.equals("*")) {
      return allStates;
    } else if (allStates.contains(state)) {
      return List.of(state);
    } else {
      throw new IllegalArgumentException("state contains unknown state.");
    }
  }

  private void saveTransitionRule(State start, Vector<Action> actionVector, State end, Double probability) {
    transitions.putIfAbsent(start, new HashMap<>());
    transitions.get(start).putIfAbsent(actionVector, new HashMap<>());
    transitions.get(start).get(actionVector).put(end, probability);
  }

  private void saveTransitionRule(State start, Vector<Action> actionVector, Map<State, Double> transitionProbabilities) {
    transitions.putIfAbsent(start, new HashMap<>());
    transitions.get(start).putIfAbsent(actionVector, new HashMap<>());
    transitions.get(start).get(actionVector).putAll(transitionProbabilities);
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
