package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPRewardType;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RewardTypeParser {
  private static final Logger LOG = LoggerFactory.getLogger(RewardTypeParser.class);

  protected DPOMDPRewardType rewardType;

  public DPOMDPRewardType getRewardType() {
    return rewardType;
  }

  public void parseRewardType(String section) {
    LOG.debug("Parsing 'value' section.");
    var match = DPOMDPSectionPattern.REWARD_TYPE
      .getMatch(section)
      .orElseThrow(() -> new IllegalArgumentException("Trying to parse value section, but found invalid format."));
    if (match.group("rewardType") != null) {
      var rewardTypeString = match.group("rewardType");
      rewardType = rewardTypeString.equals("cost") ? DPOMDPRewardType.COST : DPOMDPRewardType.REWARD;
      LOG.debug("Found reward type: {}", rewardType);
    } else {
      throw new IllegalStateException("'value' section was parsed successfully, but value could not be found.");
    }
  }
}
