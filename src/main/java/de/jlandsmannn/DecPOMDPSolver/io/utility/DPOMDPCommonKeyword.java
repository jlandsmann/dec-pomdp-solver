package de.jlandsmannn.DecPOMDPSolver.io.utility;

public enum DPOMDPCommonKeyword {
  IDENTITY("identity"),
  UNIFORM("uniform"),
  ANY("*");

  private final String keyword;

  DPOMDPCommonKeyword(String keyword) {
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
