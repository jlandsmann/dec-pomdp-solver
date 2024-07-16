package de.jlandsmannn.DecPOMDPSolver.io.utility;

import java.util.Set;

/**
 * This enum contains all the constant values of the section's keywords
 * from the .dpomdp file format and
 * provides some utility to check for those.
 */
public enum IsomorphicDPOMDPSectionKeyword implements SectionKeyword {
  PARTITION_SIZES("partitionSizes");

  public static final Set<IsomorphicDPOMDPSectionKeyword> ALL = Set.of(
    PARTITION_SIZES
  );
  private final String keyword;

  IsomorphicDPOMDPSectionKeyword(String keyword) {
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
