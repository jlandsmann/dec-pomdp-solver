package de.jlandsmannn.DecPOMDPSolver.io.utility;

import java.util.Set;

/**
 * This enum contains all the constant values of the section's keywords
 * from the .dpomdp file format and
 * provides some utility to check for those.
 */
public enum DPOMDPSectionKeyword implements SectionKeyword {
  COMMENT("#"),
  AGENTS("agents"),
  DISCOUNT("discount"),
  REWARD_TYPE("values"),
  STATES("states"),
  START("start"),
  ACTIONS("actions"),
  OBSERVATIONS("observations"),
  TRANSITION_ENTRY("T"),
  REWARD_ENTRY("R"),
  OBSERVATION_ENTRY("O");

  public static final Set<DPOMDPSectionKeyword> ALL = Set.of(
    COMMENT, AGENTS, DISCOUNT, REWARD_TYPE,
    STATES, START, ACTIONS, OBSERVATIONS,
    TRANSITION_ENTRY, REWARD_ENTRY, OBSERVATION_ENTRY
  );
  private final String keyword;

  DPOMDPSectionKeyword(String keyword) {
    this.keyword = keyword;
  }

  public String getKeyword() {
    return keyword;
  }

  public boolean isAtBeginningOf(String str) {
    return str.startsWith(keyword);
  }

  @Override
  public String toString() {
    return keyword;
  }
}
