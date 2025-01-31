package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern.*;

/**
 * This parser covers the "observations" section of the .dpomdp file format.
 * It is used to get the names of the observations with respect to their agent.
 * In case only a number of observations are defined,
 * it uses the names of the agents to create generic agent-specific observation names.
 * Therefore, it needs to know the agent's names.
 */
public class ObservationsSectionParser extends BaseSectionParser {
  private static final Logger LOG = LoggerFactory.getLogger(ObservationsSectionParser.class);

  protected List<String> agentNames = new ArrayList<>();
  protected List<List<Observation>> agentObservations = new ArrayList<>();

  public ObservationsSectionParser() {
    super(
      DPOMDPSectionKeyword.OBSERVATIONS,
      DPOMDPSectionKeyword.OBSERVATIONS + ": ?\\R" +
        "(?<agentObservations>" + ROWS_OF(OR(COUNT_PATTERN, LIST_OF(IDENTIFIER_PATTERN))) + ")"
    );
  }

  public List<List<Observation>> getAgentObservations() {
    return agentObservations;
  }

  public ObservationsSectionParser setAgentNames(List<String> agentNames) {
    this.agentNames = agentNames;
    return this;
  }

  public void parseSection(String section) {
    LOG.debug("Parsing 'observations' section.");
    assertAllDependenciesSet();
    var match = getMatchOrThrow(section);
    var rawObservationsPerAgent = match.getGroupAsStringArrayOrThrow("agentObservations", "\\R");
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

  private void assertAllDependenciesSet() {
    if (agentNames.isEmpty()) {
      throw new ParsingFailedException("'observations' section was parsed, before 'agents' have been initialized.");
    }
  }
}
