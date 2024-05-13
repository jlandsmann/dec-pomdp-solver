package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StartParser {
  private static final Logger LOG = LoggerFactory.getLogger(StartParser.class);

  protected List<State> states = new ArrayList<>();
  protected Distribution<State> initialBeliefState;

  public StartParser setStates(List<State> states) {
    this.states = states;
    return this;
  }

  public Distribution<State> getInitialBeliefState() {
    return initialBeliefState;
  }

  public void parseStart(String section) {
    LOG.debug("Parsing 'start' section.");
    if (states.isEmpty()) {
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
      var state = states.get(stateIndex);
      initialBeliefState = Distribution.createSingleEntryDistribution(state);
    }
    else if (match.group("uniformDistribution") != null) {
      initialBeliefState = Distribution.createUniformDistribution(states);
    }
    else if (match.group("distribution") != null) {
      var rawStateProbabilities = match.group("distribution");
      var rawDistribution = CommonParser.parseStatesAndTheirDistributions(states, rawStateProbabilities);
      initialBeliefState = Distribution.of(rawDistribution);
    }
    else if (match.group("includeStates") != null) {
      var rawStatesString = match.group("includeStates");
      var rawStates = rawStatesString.split(" ");
      var statesToInclude = Arrays.stream(rawStates).map(s -> {
        if (s.matches(CommonPattern.INDEX_PATTERN)) {
          var index = Integer.parseInt(s);
          return states.get(index);
        } else {
          return State.from(s);
        }
      }).toList();
      initialBeliefState = Distribution.createUniformDistribution(statesToInclude);
    }
    else if (match.group("excludeStates") != null) {
      var rawStatesString = match.group("excludeStates");
      var rawStates = rawStatesString.split(" ");
      var statesToExclude = Arrays.stream(rawStates).map(s -> {
        if (s.matches(CommonPattern.INDEX_PATTERN)) {
          var index = Integer.parseInt(s);
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
}
