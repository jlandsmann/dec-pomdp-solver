package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Optional;

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
