package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern.*;
import static de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPCommonKeyword.UNIFORM;

public class StartSectionParser extends BaseSectionParser {
  private static final Logger LOG = LoggerFactory.getLogger(StartSectionParser.class);

  protected List<State> states = new ArrayList<>();
  protected Distribution<State> initialBeliefState;

  public StartSectionParser() {
    super(
      DPOMDPSectionKeyword.START,
      DPOMDPSectionKeyword.START +
        OR(
          "(?:: ?" + "(?<startState>" + IDENTIFIER_PATTERN + ")" + ")",
          "(?:: ?" + "(?<startStateIndex>" + INDEX_PATTERN + ")" + ")",
          "(?:: ?\n" + "(?<uniformDistribution>" + UNIFORM.getPattern() + ")" + ")",
          "(?:: ?\n" + "(?<distribution>" + LIST_OF(PROBABILITY_PATTERN) + ")" + ")",
          "(?: ?include: ?" + "(?<includeStates>" + LIST_OF(OR(INDEX_PATTERN, IDENTIFIER_PATTERN)) + ")" + ")",
          "(?: ?exclude: ?" + "(?<excludeStates>" + LIST_OF(OR(INDEX_PATTERN, IDENTIFIER_PATTERN)) + ")" + ")"
        )
    );
  }

  public StartSectionParser setStates(List<State> states) {
    this.states = states;
    return this;
  }

  public Distribution<State> getInitialBeliefState() {
    return initialBeliefState;
  }

  public void parseSection(String section) {
    LOG.debug("Parsing 'start' section.");
    assertAllDependenciesSet();
    var match = getMatchOrThrow(section);
    if (match.hasGroup("startState")) {
      var stateName = match.getGroupAsStringOrThrow("startState");
      var state = State.from(stateName);
      initialBeliefState = Distribution.createSingleEntryDistribution(state);
    }
    else if (match.hasGroup("startStateIndex")) {
      var stateIndex = match.getGroupAsIntOrThrow("startStateIndex");
      var state = states.get(stateIndex);
      initialBeliefState = Distribution.createSingleEntryDistribution(state);
    }
    else if (match.hasGroup("uniformDistribution")) {
      initialBeliefState = Distribution.createUniformDistribution(states);
    }
    else if (match.hasGroup("distribution")) {
      var rawStateProbabilities = match.getGroupAsStringOrThrow("distribution");
      var rawDistribution = CommonParser.parseStatesAndTheirDistributions(states, rawStateProbabilities);
      initialBeliefState = Distribution.of(rawDistribution);
    }
    else if (match.hasGroup("includeStates")) {
      var rawStates = match.getGroupAsStringArrayOrThrow("includeStates", " ");
      var statesToInclude = Arrays.stream(rawStates).map(s -> {
        if (s.matches(CommonPattern.INDEX_PATTERN)) {
          var index = CommonParser.parseIntegerOrThrow(s);
          return states.get(index);
        } else {
          return State.from(s);
        }
      }).toList();
      initialBeliefState = Distribution.createUniformDistribution(statesToInclude);
    }
    else if (match.hasGroup("excludeStates")) {
      var rawStates = match.getGroupAsStringArrayOrThrow("excludeStates", " ");
      var statesToExclude = Arrays.stream(rawStates).map(s -> {
        if (s.matches(CommonPattern.INDEX_PATTERN)) {
          var index = CommonParser.parseIntegerOrThrow(s);
          return states.get(index);
        } else {
          return State.from(s);
        }
      }).toList();
      var statesToInclude = new ArrayList<>(states);
      statesToInclude.removeAll(statesToExclude);
      initialBeliefState = Distribution.createUniformDistribution(statesToInclude);
    }
  }

  private void assertAllDependenciesSet() {
    if (states.isEmpty()) {
      throw new ParsingFailedException("'start' section was parsed, before states have been initialized.");
    }
  }
}
