package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.IntStream;

import static de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern.*;

public class AgentsSectionParser extends BaseSectionParser {
  private final static Logger LOG = LoggerFactory.getLogger(AgentsSectionParser.class);
  protected List<String> agentNames;

  public AgentsSectionParser() {
    super(
      DPOMDPSectionKeyword.AGENTS,
      DPOMDPSectionKeyword.AGENTS + ": ?" +
      OR(
        "(?<agentCount>" + COUNT_PATTERN + ")",
        "(?<agentNames>" + LIST_OF(IDENTIFIER_PATTERN) + ")"
      ));
  }

  public List<String> getAgentNames() {
    return agentNames;
  }

  public void parseSection(String section) {
    LOG.debug("Parsing 'agents' section.");
    var match = getMatchOrThrow(section);
    if (match.hasGroup("agentCount")) {
      var agentCount = match.getGroupAsIntOrThrow("agentCount");
      if (agentCount <= 0) throw new ParsingFailedException("agentCount must be greater than zero.");
      LOG.debug("Found number of agents, creating {} agents with generic names.", agentCount);
      agentNames = IntStream.range(0, agentCount).mapToObj(i -> "A" + i).toList();
    } else if (match.hasGroup("agentNames")){
      var rawAgentNames = match.getGroupAsStringArrayOrThrow("agentNames", " ");
      agentNames = List.of(rawAgentNames);
      LOG.debug("Found custom names of agents, creating {} agents with given names.", agentNames.size());
    } else {
      throw new ParsingFailedException("'agents' section was parsed successfully, but neither agentCount nor agentNames are present.");
    }
  }
}
