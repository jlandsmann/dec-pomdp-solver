package de.jlandsmannn.DecPOMDPSolver.io.utility;

/**
 * This enum contains the constant values of the keywords from
 * the "start" section of the .dpomdp file format.
 */
public enum DPOMDPStartKeyword {
  INCLUDE("include"),
  EXCLUDE("exclude");

  private final String keyword;

  DPOMDPStartKeyword(String keyword) {
    this.keyword = keyword;
  }

  public String getKeyword() {
    return keyword;
  }

  @Override
  public String toString() {
    return keyword;
  }
}
