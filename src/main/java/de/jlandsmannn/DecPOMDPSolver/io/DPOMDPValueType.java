package de.jlandsmannn.DecPOMDPSolver.io;

public enum DPOMDPValueType {
  REWARD("reward"),
  COST("cost");

  private final String keyword;

  DPOMDPValueType(String keyword) {
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
