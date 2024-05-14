package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.IntStream;

public class AgentParser {
  private final static Logger LOG = LoggerFactory.getLogger(AgentParser.class);
  protected List<String> agentNames;

  public List<String> getAgentNames() {
    return agentNames;
  }

  public void parseAgents(String section) {
    LOG.debug("Parsing 'agents' section.");
    var match = DPOMDPSectionPattern.AGENTS
      .getMatch(section)
      .orElseThrow(() -> new ParsingFailedException("Trying to parse 'agents' section, but found invalid format."));
    if (match.group("agentCount") != null) {
      var agentCountString = match.group("agentCount");
      var agentCount = CommonParser.parseIntegerOrThrow(agentCountString);
      if (agentCount <= 0) throw new ParsingFailedException("agentCount must be greater than zero.");
      LOG.debug("Found number of agents, creating {} agents with generic names.", agentCount);
      agentNames = IntStream.range(0, agentCount).mapToObj(i -> "A" + i).toList();
    } else if (match.group("agentNames") != null){
      var rawAgentNames = match.group("agentNames");
      agentNames = List.of(rawAgentNames.split(" "));
      LOG.debug("Found custom names of agents, creating {} agents with given names.", agentNames.size());
    } else {
      throw new ParsingFailedException("'agents' section was parsed successfully, but neither agentCount nor agentNames are present.");
    }
  }
}
