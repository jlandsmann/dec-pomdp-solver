package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * This class configures some meta-properties of the {@link HeuristicPolicyIterationSolver}.
 *
 * @param beliefPointGenerationSeed    the seed for random number generation
 * @param beliefPointGenerationMaxRuns the maximum numbers of repeating belief point generations, if diversity isn't high enough
 * @param beliefPointDistanceThreshold the distance between two belief points to accept them as different belief points
 * @param valueChangeThreshold         the threshold for the DecPOMDPs value to estimate when the algorithm stagnates
 */
@ConfigurationProperties("app.heuristic-policy-iteration")
public record HeuristicPolicyIterationConfig(
  long beliefPointGenerationSeed,
  @DefaultValue("100") int beliefPointGenerationMaxRuns,
  @DefaultValue("2e-2") double beliefPointDistanceThreshold,
  @DefaultValue("1e-8") double valueChangeThreshold) {

  public static HeuristicPolicyIterationConfig getDefault() {
    return new HeuristicPolicyIterationConfig(
      12345,
      100,
      2e-3,
      1e-8
    );
  }
}
