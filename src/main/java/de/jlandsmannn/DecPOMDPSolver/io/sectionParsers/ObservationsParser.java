package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ObservationsParser {
  private static final Logger LOG = LoggerFactory.getLogger(ObservationsParser.class);

  protected List<String> agentNames = new ArrayList<>();
  protected List<List<Observation>> agentObservations = new ArrayList<>();

  public List<List<Observation>> getAgentObservations() {
    return agentObservations;
  }

  public ObservationsParser setAgentNames(List<String> agentNames) {
    this.agentNames = agentNames;
    return this;
  }

  public void parseObservations(String section) {
    LOG.debug("Parsing 'observations' section.");
    if (agentNames.isEmpty()) {
      throw new ParsingFailedException("'observations' section was parsed, before 'agents' have been initialized.");
    }

    var match = DPOMDPSectionPattern.OBSERVATIONS
      .getMatch(section)
      .orElseThrow(() -> new ParsingFailedException("Trying to parse 'observations' section, but found invalid format."));
    var rawObservations = match.group("agentObservations");
    var rawObservationsPerAgent = rawObservations.split("\n");
    if (rawObservationsPerAgent.length != agentNames.size()) {
      throw new ParsingFailedException("'observations' does not have same number of agents as 'agents' section.");
    }
    for (int i = 0; i < rawObservationsPerAgent.length; i++) {
      var rawObservationsForAgent = rawObservationsPerAgent[i];
      var agentName = agentNames.get(i);
      if (rawObservationsForAgent.matches(CommonPattern.INDEX_PATTERN)) {
        var numberOfObservations = CommonParser.parseIntegerOrThrow(rawObservationsForAgent);
        var observations = IntStream.range(0, numberOfObservations).mapToObj(idx -> agentName + "-O" + idx).map(Observation::from).toList();
        agentObservations.add(i, observations);
      } else {
        var observationNames = rawObservationsForAgent.split(" ");
        var observations = Observation.listOf(observationNames);
        agentObservations.add(i, observations);
      }
    }
  }
}
