package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;
import org.jline.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class DPOMDPFileParser {
  private static final Logger LOG = LoggerFactory.getLogger(DPOMDPFileParser.class);
  protected final DecPOMDPBuilder builder = new DecPOMDPBuilder();
  protected DPOMDPSectionKeyword currentKeyword = DPOMDPSectionKeyword.COMMENT;
  protected StringBuilder currentSectionBuilder = new StringBuilder();

  protected List<String> agentNames = new ArrayList<>();
  protected DPOMDPValueType valueType = DPOMDPValueType.REWARD;

  public static Optional<DecPOMDP<?>> parseDecPOMDP(String fileName) {
    try {
      var parser = new DPOMDPFileParser();
      var decPOMDP = parser.doParseDecPOMDP(fileName);
      return Optional.of(decPOMDP);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  protected DecPOMDP<?> doParseDecPOMDP(String fileName) throws IOException {
    var path = Path.of(fileName);
    try (var file = Files.newBufferedReader(path)) {
      var currentLine = file.readLine();
      do {
        currentLine = file.readLine();
        parseLine(currentLine);
      } while (currentLine != null);
    }
    return builder.createDecPOMDP();
  }

  protected void parseLine(String currentLine) {
    if (currentLine == null) {
      Log.info("Reached end of file, finishing current section and parsing it.");
      parseCurrentSection();
      return;
    } else if (DPOMDPSectionKeyword.COMMENT.isAtBeginningOf(currentLine)) {
      Log.debug("Found comment line, ignoring it.");
      return;
    }
    var keywordMatching = DPOMDPSectionKeyword.ALL
      .stream()
      .filter(keyword -> keyword.isAtBeginningOf(currentLine))
      .findFirst();

    if (keywordMatching.isPresent()) {
      Log.debug("Found keyword at beginning of line, finishing current section and parsing it.");
      parseCurrentSection();
      startNewSection(keywordMatching.get());
    }
    Log.debug("No keyword matched current line, adding line to current section.");
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
      case DPOMDPSectionKeyword.VALUES: parseValue(section);
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
    Log.debug("Parsing 'agents' section.");
    var matcher = DPOMDPSectionKeyword.AGENTS.getMatcher(section);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Trying to parse agents section, but found invalid format.");
    }
    if (matcher.namedGroups().containsKey("agentCount")) {
      var agentCountString = matcher.group("agentCount");
      var agentCount = Integer.parseInt(agentCountString);
      Log.debug("Found number of agents, creating {} agents with generic names.", agentCount);
      agentNames = IntStream.range(0, agentCount).mapToObj(i -> "A" + i).toList();
    } else {
      var agentCount = matcher.groupCount();
      Log.debug("Found custom names of agents defined, creating {} agents with given names.", agentCount);
      agentNames = IntStream.range(0, agentCount).mapToObj(matcher::group).toList();
    }
  }

  protected void parseDiscount(String section) {
    Log.debug("Parsing 'discount' section.");
  }

  protected void parseValue(String section) {
    Log.debug("Parsing 'value' section.");

  }

  protected void parseStates(String section) {
    Log.debug("Parsing 'states' section.");

  }

  protected void parseStart(String section) {
    Log.debug("Parsing 'start' section.");

  }

  protected void parseActions(String section) {
    Log.debug("Parsing 'actions' section.");

  }

  protected void parseObservations(String section) {
    Log.debug("Parsing 'observations' section.");
  }

  protected void parseTransitionEntry(String section) {
    Log.debug("Parsing 'T' section.");

  }

  protected void parseRewardEntry(String section) {
    Log.debug("Parsing 'R' section.");
  }

  protected void parseObservationEntry(String section) {
    Log.debug("Parsing 'O' section.");
  }
}
