package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPRewardType;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.IntStream;

public class StatesParser {
  private static final Logger LOG = LoggerFactory.getLogger(RewardTypeParser.class);

  protected List<State> states;

  public List<State> getStates() {
    return states;
  }

  public void parseStates(String section) {
    LOG.debug("Parsing 'states' section.");
    var match = DPOMDPSectionPattern.STATES
      .getMatch(section)
      .orElseThrow(() -> new ParsingFailedException("Trying to parse 'states' section, but found invalid format."));
    if (match.group("stateCount") != null) {
      var stateCountString = match.group("stateCount");
      var stateCount = CommonParser.parseIntegerOrThrow(stateCountString);
      if (stateCount <= 0) throw new ParsingFailedException("stateCount must be greater than zero.");
      LOG.debug("Found number of states, creating {} states with generic names.", stateCount);
      states = IntStream.range(0, stateCount)
        .mapToObj(i -> "S" + i)
        .map(State::from)
        .toList();
    } else if (match.group("stateNames") != null){
      var rawStateNames = match.group("stateNames");
      states = State.listOf(rawStateNames.split(" "));
      LOG.debug("Found custom names of states, creating {} states with given names.", states.size());
    } else {
      throw new ParsingFailedException("'states' section was parsed successfully, but neither stateCount nor stateNames are present.");
    }
  }
}
