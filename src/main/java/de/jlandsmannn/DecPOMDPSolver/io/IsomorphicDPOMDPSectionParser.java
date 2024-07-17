package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.domain.lifting.ILiftedAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.io.sectionParsers.PartitionSizesSectionParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.IsomorphicDPOMDPSectionKeyword;
import de.jlandsmannn.DecPOMDPSolver.io.utility.SectionKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class IsomorphicDPOMDPSectionParser<BUILDER extends IsomorphicDecPOMDPWithStateControllerBuilder> extends DPOMDPSectionParser<BUILDER> implements ISectionParser {
  private static final Logger LOG = LoggerFactory.getLogger(IsomorphicDPOMDPSectionParser.class);

  private List<Integer> partitionSizes = List.of();

  public IsomorphicDPOMDPSectionParser(BUILDER builder) {
    super(builder);
  }

  @Override
  public Set<SectionKeyword> getSectionKeywords() {
    var keywords = super.getSectionKeywords();
    keywords.addAll(IsomorphicDPOMDPSectionKeyword.ALL);
    return keywords;
  }

  @Override
  public void parseSection(SectionKeyword keyword, String section) {
    if (keyword instanceof IsomorphicDPOMDPSectionKeyword) {
      switch ((IsomorphicDPOMDPSectionKeyword) keyword) {
        case PARTITION_SIZES -> parsePartitionSizes(section);
      }
    } else {
      super.parseSection(keyword, section);
    }
  }

  private void parsePartitionSizes(String section) {
    LOG.debug("Parsing 'partitionSizes' section.");
    var parser = new PartitionSizesSectionParser();
    parser.setAgentNames(agentNames).parseSection(section);
    partitionSizes = parser.getPartitionSizes();
  }

  @Override
  protected void gatherAgentsAndAddToBuilder() {
    for (int i = 0; i < agentNames.size(); i++) {
      var name = agentNames.get(i);
      var actions = agentActions.get(i);
      var observations = agentObservations.get(i);
      var partitionSize = partitionSizes.get(i);
      ILiftedAgent agent = builder.getAgentBuilder()
        .setName(name)
        .setActions(actions)
        .setObservations(observations)
        .setPartitionSize(partitionSize)
        .createAgent();
      builder.addAgent(agent);
    }
  }
}
