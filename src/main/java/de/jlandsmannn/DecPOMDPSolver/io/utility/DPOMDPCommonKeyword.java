package de.jlandsmannn.DecPOMDPSolver.io.utility;

public enum DPOMDPCommonKeyword {
  IDENTITY("identity"),
  UNIFORM("uniform"),
  ANY("*", "\\*");

  private final String keyword;
  private final String pattern;

  DPOMDPCommonKeyword(String keyword) {
    this(keyword, keyword);
  }
  DPOMDPCommonKeyword(String keyword, String pattern) {
    this.keyword = keyword;
    this.pattern = pattern;
  }

  public String getKeyword() {
    return keyword;
  }

  @Override
  public String toString() {
    return keyword;
  }

  public String getPattern() {
    return pattern;
  }
}
