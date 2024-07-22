package de.jlandsmannn.DecPOMDPSolver.isomorphicPolicyIteration;

import de.jlandsmannn.DecPOMDPSolver.policyIteration.HeuristicPolicyIterationConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * This class configures some meta-properties of the {@link IsomorphicHeuristicPolicyIterationSolver}.
 *
 * @param policyIterationConfig the parameters to configure the policy iteration algorithm used internally
 */
@ConfigurationProperties("app.isomorhpic-heuristic-policy-iteration")
public record IsomorphicHeuristicPolicyIterationConfig(
  @DefaultValue HeuristicPolicyIterationConfig policyIterationConfig,
  @DefaultValue("false") boolean representativeObservations){

  public static IsomorphicHeuristicPolicyIterationConfig getDefault() {
    return new IsomorphicHeuristicPolicyIterationConfig(
      HeuristicPolicyIterationConfig.getDefault(),
      false
    );
  }

  public IsomorphicHeuristicPolicyIterationConfig withPolicyIterationConfig(HeuristicPolicyIterationConfig config) {
    return new IsomorphicHeuristicPolicyIterationConfig(config, false);
  }
}
