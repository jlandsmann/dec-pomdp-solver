package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPRewardType;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This parser covers the "values" section of the .dpomdp file format.
 * That section declares whether the given rewards should be interpreted
 * as desirable rewards or as costs to be avoided.
 * These cases are defined by the {@link DPOMDPRewardType}.
 */
public class RewardTypeSectionParser extends BaseSectionParser {
  private static final Logger LOG = LoggerFactory.getLogger(RewardTypeSectionParser.class);

  protected DPOMDPRewardType rewardType;

  public RewardTypeSectionParser() {
    super(
      DPOMDPSectionKeyword.REWARD_TYPE,
      DPOMDPSectionKeyword.REWARD_TYPE + ": ?" + "(?<rewardType>reward|cost)"
    );
  }

  public DPOMDPRewardType getRewardType() {
    return rewardType;
  }

  public void parseSection(String section) {
    LOG.debug("Parsing 'value' section.");
    var match = getMatchOrThrow(section);
    if (match.hasGroup("rewardType")) {
      var rewardTypeString = match.getGroupAsStringOrThrow("rewardType");
      rewardType = rewardTypeString.equals("cost") ? DPOMDPRewardType.COST : DPOMDPRewardType.REWARD;
      LOG.debug("Found reward type: {}", rewardType);
    } else {
      throw new ParsingFailedException("'value' section was parsed successfully, but value could not be found.");
    }
  }
}
