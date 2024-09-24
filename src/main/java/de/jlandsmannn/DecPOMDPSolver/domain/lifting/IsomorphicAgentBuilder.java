package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.AgentBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

public class IsomorphicAgentBuilder<THIS extends IsomorphicAgentBuilder<?>> extends AgentBuilder<IsomorphicAgentWithStateController, THIS> {

  private int partitionSize = 1;
  protected FiniteStateController stateController;

  public THIS setPartitionSize(int partitionSize) {
    this.partitionSize = partitionSize;
    return (THIS) this;
  }

  public IsomorphicAgentBuilder withFirstActionInitialPolicy() {
    var policy = Distribution.createSingleEntryDistribution(actions.get(0));
    return withInitialPolicy(policy);
  }

  public IsomorphicAgentBuilder withUniformInitialPolicy() {
    var policy = Distribution.createUniformDistribution(actions);
    return withInitialPolicy(policy);
  }

  public IsomorphicAgentBuilder withRandomInitialPolicy() {
    var policy = Distribution.createRandomDistribution(actions);
    return withInitialPolicy(policy);
  }

  public IsomorphicAgentBuilder withInitialPolicy(Distribution<Action> initialPolicy) {
    stateController = FiniteStateControllerBuilder.createSelfLoopController(name, actions, observations, initialPolicy);
    return this;
  }

  @Override
  public IsomorphicAgentWithStateController createAgent() {
      if (stateController == null) withFirstActionInitialPolicy();
    return new IsomorphicAgentWithStateController(name, actions, observations, stateController, partitionSize);
  }
}
