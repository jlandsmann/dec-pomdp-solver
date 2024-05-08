package de.jlandsmannn.DecPOMDPSolver.io;

public enum DPOMDPStateKeyword {
  INCLUDE("include"),
  EXCLUDE("exclude");

  private final String keyword;

  DPOMDPStateKeyword(String keyword) {
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
