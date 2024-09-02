package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Map;

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
  @DefaultValue("0") long beliefPointGenerationSeed,
  @DefaultValue("10") int beliefPointDesiredNumber,
  @DefaultValue("10") int beliefPointGenerationMaxRuns,
  @DefaultValue("2e-2") double beliefPointDistanceThreshold,
  @DefaultValue("1e-8") double valueChangeThreshold,
  @DefaultValue("0") int maxIterations,
  Map<IAgent, Map<State, Distribution<Action>>> initialPolicies) {

  public static HeuristicPolicyIterationConfig getDefault() {
    return new HeuristicPolicyIterationConfig(
      0,
      10,
      10,
      2e-3,
      1e-8,
      0,
      null
    );
  }

  public HeuristicPolicyIterationConfig withMaxIterations(int maxIterations) {
    if (maxIterations <= 0) return this;
    return new HeuristicPolicyIterationConfig(
      beliefPointGenerationSeed(),
      beliefPointDesiredNumber(),
      beliefPointGenerationMaxRuns(),
      beliefPointDistanceThreshold(),
      valueChangeThreshold(),
      maxIterations,
      initialPolicies()
    );
  }

  public HeuristicPolicyIterationConfig withNumberOfBeliefPoints(int numberOfBeliefPoints) {
    if (numberOfBeliefPoints <= 0) return this;
    return new HeuristicPolicyIterationConfig(
      beliefPointGenerationSeed(),
      numberOfBeliefPoints,
      beliefPointGenerationMaxRuns(),
      beliefPointDistanceThreshold(),
      valueChangeThreshold(),
      maxIterations(),
      initialPolicies()
    );
  }

  public HeuristicPolicyIterationConfig withInitialPolicies(Map<IAgent, Map<State, Distribution<Action>>> initialPolicies) {
    return new HeuristicPolicyIterationConfig(
      beliefPointGenerationSeed(),
      beliefPointDesiredNumber(),
      beliefPointGenerationMaxRuns(),
      beliefPointDistanceThreshold(),
      valueChangeThreshold(),
      maxIterations(),
      initialPolicies
    );
  }
}
