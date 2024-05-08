package de.jlandsmannn.DecPOMDPSolver.io.utility;

import org.springframework.util.PatternMatchUtils;

import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern.*;


public enum DPOMDPSectionPattern {
  COMMENT(DPOMDPSectionKeyword.COMMENT + ".*"),
  AGENTS(
    DPOMDPSectionKeyword.AGENTS + ": " +
      "(?:" +
      "(?<agentCount>" + POSITIVE_INTEGER_PATTERN + ")" +
      "|" +
      "(?<agentNames>" + "(?:" + NAME_PATTERN + " ?)" + "+)" +
      ")"
  ),
  DISCOUNT(
    DPOMDPSectionKeyword.DISCOUNT + ": " +
      "(?<discount>" + POSITIVE_NUMBER_PATTERN + ")"
  ),
  REWARD_TYPE(
    DPOMDPSectionKeyword.REWARD_TYPE + ": "+
      "(?<rewardType>reward|cost)"
  ),
  STATES(
    DPOMDPSectionKeyword.STATES + ": " +
      "(?:" +
      "(?<stateCount>" + POSITIVE_INTEGER_PATTERN + ")" +
      "|" +
      "(?<stateNames>" + "(?:" + NAME_PATTERN + " ?)" + "+)" +
      ")"),
  START(),
  ACTIONS(),
  OBSERVATIONS(),
  TRANSITION_ENTRY(),
  REWARD_ENTRY(),
  OBSERVATION_ENTRY();

  private final Pattern pattern;

  DPOMDPSectionPattern() {
    this(Pattern.compile(".*"));
  }

  DPOMDPSectionPattern(String pattern) {
    this(Pattern.compile(pattern));
  }

  DPOMDPSectionPattern(Pattern pattern) {
    this.pattern = pattern;
  }

  public Optional<MatchResult> getMatch(String section) {
    var matcher = pattern.matcher(section);
    if (!matcher.find() || !matcher.matches()) return Optional.empty();
    return Optional.of(matcher.toMatchResult());
  }

  @Override
  public String toString() {
    return pattern.toString();
  }
}
