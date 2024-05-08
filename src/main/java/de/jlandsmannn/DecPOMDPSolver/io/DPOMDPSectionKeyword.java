package de.jlandsmannn.DecPOMDPSolver.io;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.jlandsmannn.DecPOMDPSolver.io.CommonPattern.NAME_PATTERN;
import static de.jlandsmannn.DecPOMDPSolver.io.CommonPattern.NUMBER_PATTERN;


public enum DPOMDPSectionKeyword {
  COMMENT("#", ".*"),
  AGENTS("agents", "agents: (?:(?<agentCount>" + NUMBER_PATTERN + ")|(" + NAME_PATTERN + ")(?: (" + NAME_PATTERN + "))+)"),
  DISCOUNT("discount", ".*"),
  VALUES("values", ".*"),
  STATES("states", ".*"),
  START("start", ".*"),
  ACTIONS("actions", ".*"),
  OBSERVATIONS("observations", ".*"),
  TRANSITION_ENTRY("T", ".*"),
  REWARD_ENTRY("R", ".*"),
  OBSERVATION_ENTRY("O", ".*");
  private final String keyword;
  private final Pattern regexp;

  DPOMDPSectionKeyword(String keyword, String regexp) {
    this(keyword, Pattern.compile(regexp));
  }

  DPOMDPSectionKeyword(String keyword, Pattern regexp) {
    this.keyword = keyword;
    this.regexp = regexp;
  }

  public String getKeyword() {
    return keyword;
  }

  public boolean isAtBeginningOf(String str) {
    return str.startsWith(keyword);
  }

  public Matcher getMatcher(String section) {
    return regexp.matcher(section);
  }

  @Override
  public String toString() {
    return keyword;
  }

  public static final Set<DPOMDPSectionKeyword> ALL = Set.of(
    COMMENT, AGENTS, DISCOUNT, VALUES,
    STATES, START, ACTIONS, OBSERVATIONS,
    TRANSITION_ENTRY, REWARD_ENTRY, OBSERVATION_ENTRY
  );
}
