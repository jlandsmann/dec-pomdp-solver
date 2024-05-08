package de.jlandsmannn.DecPOMDPSolver.io.utility;

public enum DPOMDPRewardType {
  REWARD("reward"),
  COST("cost");

  private final String keyword;

  DPOMDPRewardType(String keyword) {
    this.keyword = keyword;
  }

  public static DPOMDPRewardType parse(String keyword) {
    if (keyword.equals("reward")) {
      return REWARD;
    } else if (keyword.equals("cost")) {
      return COST;
    }
    throw new IllegalArgumentException("Unknown DPOMDPRewardType: " + keyword);
  }

  public String getKeyword() {
    return keyword;
  }

  @Override
  public String toString() {
    return keyword;
  }
}
