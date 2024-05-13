package de.jlandsmannn.DecPOMDPSolver.io.utility;

import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern.*;
import static de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPCommonKeyword.*;


public enum DPOMDPSectionPattern {
  COMMENT(DPOMDPSectionKeyword.COMMENT + ".*"),
  AGENTS(
    DPOMDPSectionKeyword.AGENTS + ": " +
      OR(
        "(?<agentCount>" + POSITIVE_INTEGER_PATTERN + ")",
        "(?<agentNames>" + LIST_OF(IDENTIFIER_PATTERN) + ")"
      )
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
      OR(
        "(?<stateCount>" + POSITIVE_INTEGER_PATTERN + ")",
        "(?<stateNames>" + LIST_OF(IDENTIFIER_PATTERN) + ")"
      )
  ),
  START(
    DPOMDPSectionKeyword.START +
      OR(
        "(?:: " + "(?<startState>" + IDENTIFIER_PATTERN + ")" + ")",
          "(?:: " + "(?<startStateIndex>" + POSITIVE_INTEGER_PATTERN + ")" + ")",
          "(?:: ?\n" + "(?<uniformDistribution>" + UNIFORM.getPattern() + ")" + ")",
          "(?:: ?\n" + "(?<distribution>" + LIST_OF(POSITIVE_NUMBER_PATTERN) + ")" + ")",
          "(?: include: " + "(?<includeStates>" + LIST_OF(OR(POSITIVE_INTEGER_PATTERN, IDENTIFIER_PATTERN)) + ")" + ")",
          "(?: exclude: " + "(?<excludeStates>" + LIST_OF(OR(POSITIVE_INTEGER_PATTERN, IDENTIFIER_PATTERN)) + ")" + ")"
      )
  ),
  ACTIONS(
    DPOMDPSectionKeyword.ACTIONS + ": ?\n" +
      "(?<agentActions>" + ROWS_OF(OR(POSITIVE_INTEGER_PATTERN, LIST_OF(IDENTIFIER_PATTERN))) + ")"
  ),
  OBSERVATIONS(
    DPOMDPSectionKeyword.OBSERVATIONS + ": ?\n" +
      "(?<agentObservations>" + ROWS_OF(OR(POSITIVE_INTEGER_PATTERN, LIST_OF(IDENTIFIER_PATTERN))) + ")"
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
