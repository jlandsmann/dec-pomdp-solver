package de.jlandsmannn.DecPOMDPSolver.io.utility;

import java.util.Set;


public enum DPOMDPSectionKeyword {
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

  public static final Set<DPOMDPSectionKeyword> ALL = Set.of(
    COMMENT, AGENTS, DISCOUNT, REWARD_TYPE,
    STATES, START, ACTIONS, OBSERVATIONS,
    TRANSITION_ENTRY, REWARD_ENTRY, OBSERVATION_ENTRY
  );
}
