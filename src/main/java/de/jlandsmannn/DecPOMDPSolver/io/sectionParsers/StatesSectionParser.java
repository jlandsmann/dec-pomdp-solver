package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.IntStream;

import static de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern.*;

/**
 * This parser covers the "states" section of the .dpomdp file format.
 * That section describes which states exist in the DecPOMDP.
 * Either the number of states or a list of state names is defined.
 */
public class StatesSectionParser extends BaseSectionParser {
  private static final Logger LOG = LoggerFactory.getLogger(RewardTypeSectionParser.class);

  protected List<State> states;

  public StatesSectionParser() {
    super(
      DPOMDPSectionKeyword.STATES,
      DPOMDPSectionKeyword.STATES + ": ?" +
        OR(
          "(?<stateCount>" + COUNT_PATTERN + ")",
          "(?<stateNames>" + LIST_OF(IDENTIFIER_PATTERN) + ")"
        )
    );
  }

  public List<State> getStates() {
    return states;
  }

  public void parseSection(String section) {
    LOG.debug("Parsing 'states' section.");
    var match = getMatchOrThrow(section);
    if (match.hasGroup("stateCount")) {
      var stateCount = match.getGroupAsIntOrThrow("stateCount");
      if (stateCount <= 0) throw new ParsingFailedException("stateCount must be greater than zero.");
      LOG.debug("Found number of states, creating {} states with generic names.", stateCount);
      states = IntStream.range(0, stateCount)
        .mapToObj(i -> "S" + i)
        .map(State::from)
        .toList();
    } else if (match.hasGroup("stateNames")){
      var stateNames = match.getGroupAsStringArrayOrThrow("stateNames", " ");
      states = State.listOf(stateNames);
      LOG.debug("Found custom names of states, creating {} states with given names.", states.size());
    } else {
      throw new ParsingFailedException("'states' section was parsed successfully, but neither stateCount nor stateNames are present.");
    }
  }
}
