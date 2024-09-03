package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.AgentBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateControllerBuilder;

public class IsomorphicAgentBuilder<THIS extends IsomorphicAgentBuilder<?>> extends AgentBuilder<IsomorphicAgentWithStateController, THIS> {

  private int partitionSize = 1;
  protected FiniteStateController stateController;

  public THIS setPartitionSize(int partitionSize) {
    this.partitionSize = partitionSize;
    return (THIS) this;
  }

  public THIS setStateController(FiniteStateController stateController) {
    this.stateController = stateController;
    return (THIS) this;
  }

  @Override
  public IsomorphicAgentWithStateController createAgent() {
    setControllerIfNotSet();
    return new IsomorphicAgentWithStateController(name, actions, observations, stateController, partitionSize);
  }

  protected void setControllerIfNotSet() {
    if (stateController == null) {
      stateController = FiniteStateControllerBuilder.createArbitraryController(name, actions, observations);
    }
  }
}
