package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.IsomorphicDPOMDPSectionKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern.COUNT_PATTERN;
import static de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern.ROWS_OF;

/**
 * This parser covers the "partitionSizes" section.
 * This is an extension to the '.dpomdp' file format
 * to support isomorphic DecPOMDPs.
 * It is used to get the number of agents in a partition.
 */
public class PartitionSizesSectionParser extends BaseSectionParser {
  private final static Logger LOG = LoggerFactory.getLogger(PartitionSizesSectionParser.class);

  protected List<String> agentNames = new ArrayList<>();
  protected List<Integer> partitionSizes;

  public PartitionSizesSectionParser() {
    super(
      IsomorphicDPOMDPSectionKeyword.PARTITION_SIZES,
      IsomorphicDPOMDPSectionKeyword.PARTITION_SIZES + ": ?\\R" +
        "(?<partitionSizes>" + ROWS_OF(COUNT_PATTERN) + ")");
  }

  public PartitionSizesSectionParser setAgentNames(List<String> agentNames) {
    this.agentNames = agentNames;
    return this;
  }

  public List<Integer> getPartitionSizes() {
    return partitionSizes;
  }

  public void parseSection(String section) {
    LOG.debug("Parsing 'partitionSizes' section.");
    var match = getMatchOrThrow(section);
    if (match.hasGroup("partitionSizes")) {
      var rawPartitionCounts = match.getGroupAsStringArrayOrThrow("partitionSizes", "\\R");
      if (rawPartitionCounts.length != agentNames.size()) {
        throw new ParsingFailedException("'partitionSizes' does not have same number of partitions as 'agents' section.");
      }
      partitionSizes = Arrays.stream(rawPartitionCounts).map(CommonParser::parseIntegerOrThrow).toList();
      if (partitionSizes.stream().anyMatch(p -> p <= 0)) {
        throw new ParsingFailedException("Some partition is empty, which is not allowed");
      }
    } else {
      throw new ParsingFailedException("'agents' section was parsed successfully, but neither agentCount nor agentNames are present.");
    }
  }
}
