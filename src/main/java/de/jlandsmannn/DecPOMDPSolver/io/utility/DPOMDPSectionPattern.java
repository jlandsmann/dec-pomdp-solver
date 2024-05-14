package de.jlandsmannn.DecPOMDPSolver.io.utility;

import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern.*;
import static de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern.NAMED_GROUP;
import static de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPCommonKeyword.*;


public enum DPOMDPSectionPattern {
  COMMENT(DPOMDPSectionKeyword.COMMENT + ".*"),
  AGENTS(
    DPOMDPSectionKeyword.AGENTS + ": ?" +
      OR(
        "(?<agentCount>" + COUNT_PATTERN + ")",
        "(?<agentNames>" + LIST_OF(IDENTIFIER_PATTERN) + ")"
      )
  ),
  DISCOUNT(
    DPOMDPSectionKeyword.DISCOUNT + ": ?" +
      "(?<discount>" + PROBABILITY_PATTERN + ")"
  ),
  REWARD_TYPE(
    DPOMDPSectionKeyword.REWARD_TYPE + ": ?" +
      "(?<rewardType>reward|cost)"
  ),
  STATES(
    DPOMDPSectionKeyword.STATES + ": ?" +
      OR(
        "(?<stateCount>" + COUNT_PATTERN + ")",
        "(?<stateNames>" + LIST_OF(IDENTIFIER_PATTERN) + ")"
      )
  ),
  START(
    DPOMDPSectionKeyword.START +
      OR(
        "(?:: ?" + "(?<startState>" + IDENTIFIER_PATTERN + ")" + ")",
        "(?:: ?" + "(?<startStateIndex>" + INDEX_PATTERN + ")" + ")",
        "(?:: ?\n" + "(?<uniformDistribution>" + UNIFORM.getPattern() + ")" + ")",
        "(?:: ?\n" + "(?<distribution>" + LIST_OF(PROBABILITY_PATTERN) + ")" + ")",
        "(?: ?include: ?" + "(?<includeStates>" + LIST_OF(OR(INDEX_PATTERN, IDENTIFIER_PATTERN)) + ")" + ")",
        "(?: ?exclude: ?" + "(?<excludeStates>" + LIST_OF(OR(INDEX_PATTERN, IDENTIFIER_PATTERN)) + ")" + ")"
      )
  ),
  ACTIONS(
    DPOMDPSectionKeyword.ACTIONS + ": ?\n" +
      "(?<agentActions>" + ROWS_OF(OR(COUNT_PATTERN, LIST_OF(IDENTIFIER_PATTERN))) + ")"
  ),
  OBSERVATIONS(
    DPOMDPSectionKeyword.OBSERVATIONS + ": ?\n" +
      "(?<agentObservations>" + ROWS_OF(OR(COUNT_PATTERN, LIST_OF(IDENTIFIER_PATTERN))) + ")"
  ),
  TRANSITION_ENTRY(
    DPOMDPSectionKeyword.TRANSITION_ENTRY + ": ?" +
      NAMED_GROUP("actionVector", LIST_OF(OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern()))) +
      " ?: ?" +
      OR(
        // start state defined for given action vector
        NAMED_GROUP("startState", OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern())) +
          " ?: ?" +
          OR(
            // end state and probability explicitly given
            NAMED_GROUP("endState", OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern())) +
              " ?: ?" + NAMED_GROUP("probability", PROBABILITY_PATTERN),
            // probability distribution given explicitly
            "\n" + NAMED_GROUP("probabilityDistribution", LIST_OF(PROBABILITY_PATTERN))
          ),
        // uniform probability distribution
        "\n" + NAMED_GROUP("probabilityUniformDistribution", UNIFORM.getPattern()),
        // end state = start state‚
        "\n" + NAMED_GROUP("probabilityIdentityDistribution", IDENTITY.getPattern()),
        // matrix defined for given action vector
        "\n" + NAMED_GROUP("probabilityMatrix", ROWS_OF(LIST_OF(PROBABILITY_PATTERN)))
      )
  ),
  OBSERVATION_ENTRY(
    DPOMDPSectionKeyword.OBSERVATION_ENTRY + ": ?" +
      NAMED_GROUP("actionVector", LIST_OF(OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern()))) +
      " ?: ?" +
      OR(
        NAMED_GROUP("endState", OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern())) +
          " ?: ?" +
          OR(
            NAMED_GROUP("observationVector", LIST_OF(OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern()))) +
              " ?: ?" + NAMED_GROUP("probability", PROBABILITY_PATTERN),
            "\n" + NAMED_GROUP("probabilityDistribution", LIST_OF(PROBABILITY_PATTERN))
          ),
        // uniform probability distribution
        "\n" + NAMED_GROUP("probabilityUniformDistribution", UNIFORM.getPattern()),
        // matrix defined for given action vector
        "\n" + NAMED_GROUP("probabilityMatrix", ROWS_OF(LIST_OF(PROBABILITY_PATTERN)))
      )
  ),
  REWARD_ENTRY(
    DPOMDPSectionKeyword.REWARD_ENTRY + ": ?" +
      NAMED_GROUP("actionVector", LIST_OF(OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern()))) +
      " ?: ?" +
      NAMED_GROUP("startState", OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern())) +
      " ?: ?" +
      OR(
        NAMED_GROUP("endState", OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern())) +
          " ?: ?" +
          OR(
            NAMED_GROUP("observationVector", LIST_OF(OR(IDENTIFIER_PATTERN, INDEX_PATTERN, ANY.getPattern()))) +
              " ?: ?" + NAMED_GROUP("reward", NUMBER_PATTERN),
            "\n" + NAMED_GROUP("rewardDistribution", LIST_OF(NUMBER_PATTERN))
          ),
        // matrix defined for given action vector
        "\n" + NAMED_GROUP("rewardMatrix", ROWS_OF(LIST_OF(NUMBER_PATTERN)))
      )

  );

  private final Pattern pattern;

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
