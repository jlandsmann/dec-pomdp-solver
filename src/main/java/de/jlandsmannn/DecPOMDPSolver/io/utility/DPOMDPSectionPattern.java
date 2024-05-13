package de.jlandsmannn.DecPOMDPSolver.io.utility;

import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern.*;
import static de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPCommonKeyword.UNIFORM;


public enum DPOMDPSectionPattern {
  COMMENT(DPOMDPSectionKeyword.COMMENT + ".*"),
  AGENTS(
    DPOMDPSectionKeyword.AGENTS + ": " +
      "(?:" +
      "(?<agentCount>" + POSITIVE_INTEGER_PATTERN + ")" +
      "|" +
      "(?<agentNames>" + LIST_OF_IDENTIFIERS + ")" +
      ")"
  ),
  DISCOUNT(
    DPOMDPSectionKeyword.DISCOUNT + ": " +
      "(?<discount>" + POSITIVE_NUMBER_PATTERN + ")"
  ),
  REWARD_TYPE(
    DPOMDPSectionKeyword.REWARD_TYPE + ": " +
      "(?<rewardType>reward|cost)"
  ),
  STATES(
    DPOMDPSectionKeyword.STATES + ": " +
      "(?:" +
      "(?<stateCount>" + POSITIVE_INTEGER_PATTERN + ")" +
      "|" +
      "(?<stateNames>" + LIST_OF_IDENTIFIERS + ")" +
      ")"),
  START(
    DPOMDPSectionKeyword.START + "(?:" +
      "(?:: " + "(?<startState>" + IDENTIFIER_PATTERN + ")" + ")" +
      "|" +
      "(?:: " + "(?<startStateIndex>" + POSITIVE_INTEGER_PATTERN + ")" + ")" +
      "|" +
      "(?:: ?\n" + "(?<uniformDistribution>" + UNIFORM + ")" + ")" +
      "|" +
      "(?:: ?\n" + "(?<distribution>" + "(?:" + POSITIVE_NUMBER_PATTERN + " ?)" + "+)" + ")" +
      "|" +
      "(?: include: " + "(?<includeStates>" + "(?:" + "(?:" + IDENTIFIER_PATTERN + "|" + POSITIVE_INTEGER_PATTERN + ")" + " ?)" + "+)" + ")" +
      "|" +
      "(?: exclude: " + "(?<excludeStates>" + "(?:" + "(?:" + IDENTIFIER_PATTERN + "|" + POSITIVE_INTEGER_PATTERN + ")" + " ?)" + "+)" + ")" +
      ")"
  ),
  ACTIONS(
    DPOMDPSectionKeyword.ACTIONS + ": ?\n" + "(?<agentActions>" +
      "(?:" +
      "(?:" +
      "(?:" + POSITIVE_NUMBER_PATTERN + ")" +
      "|" +
      "(?:" + LIST_OF_IDENTIFIERS + ")" +
      ")" +
      " ?\n)*" +
      "(?:" +
      "(?:" + POSITIVE_NUMBER_PATTERN + ")" +
      "|" +
      "(?:" + LIST_OF_IDENTIFIERS + ")" +
      ") ?" +
      ")"
  ),
  OBSERVATIONS(
    DPOMDPSectionKeyword.OBSERVATIONS + ": ?\n" + "(?<agentObservations>" +
      "(?:" +
      "(?:" +
      "(?:" + POSITIVE_NUMBER_PATTERN + ")" +
      "|" +
      "(?:" + LIST_OF_IDENTIFIERS + ")" +
      ")" +
      " ?\n)*" +
      "(?:" +
      "(?:" + POSITIVE_NUMBER_PATTERN + ")" +
      "|" +
      "(?:" + LIST_OF_IDENTIFIERS + ")" +
      ") ?" +
      ")"
  ),
  TRANSITION_ENTRY(),
  REWARD_ENTRY(),
  OBSERVATION_ENTRY();

  private final Pattern pattern;

  DPOMDPSectionPattern() {
    this(".*");
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
