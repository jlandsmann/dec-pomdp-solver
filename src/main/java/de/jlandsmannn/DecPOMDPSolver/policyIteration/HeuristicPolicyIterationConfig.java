package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.heuristic-policy-iteration")
public record HeuristicPolicyIterationConfig(
  @DefaultValue("100") int maxBeliefPointGenerationRuns,
  @DefaultValue("2e-2") double beliefPointDistanceThreshold,
  @DefaultValue("1e-8") double valueChangeThreshold) {

  public static HeuristicPolicyIterationConfig getDefault() {
    return new HeuristicPolicyIterationConfig(
      100,
      2e-3,
      1e-8
    );
  }
}
